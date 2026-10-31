package com.viewer.judge.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Volume;
import com.viewer.common.core.constants.JudgeConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 判题容器池：启动时预先创建若干容器，判题时借出、判题完归还。
 */
@Slf4j
public class DockerSandBoxPool {

    /**
     * 借出容器时最长等待时间（秒）：池中没有可用容器时快速失败，避免判题线程无限期阻塞。
     */
    private static final long BORROW_TIMEOUT_SECONDS = 30L;

    /**
     * 单个容器被使用多少次后重建。
     * Docker 的 memory_stats.max_usage 是容器生命周期内的累计峰值，只有重建容器才会清零；
     * 池化复用容器时若不定期重建，前一个用户的内存峰值会被算到后一个用户头上，导致内存误判。
     */
    private static final int RECYCLE_AFTER_USES = 20;

    /**
     * 池子巡检间隔（秒）：定时检查并补建缺失的容器，避免容器被 OOM-kill 后池子越用越小。
     */
    private static final long INSPECT_INTERVAL_SECONDS = 60L;

    private DockerClient dockerClient;

    private String sandboxImage;

    private String volumeDir;

    private Long memoryLimit;

    private Long memorySwapLimit;

    private Long cpuLimit;

    private int poolSize;

    private String containerNamePrefix;

    private BlockingQueue<String> containerQueue;

    private Map<String, String> containerNameMap;

    /** 每个容器的累计使用次数，达到阈值后在归还时重建 */
    private Map<String, Integer> containerUseCount;

    /** 已借出、尚未归还的容器，用于识别重复归还，避免同一个容器被同时借给两个判题线程 */
    private Set<String> leasedContainers;

    /** 容器创建/重建互斥锁，避免巡检线程与判题线程同时创建同名容器 */
    private final Object createLock = new Object();

    /** 池子巡检线程：定时补建缺失的容器 */
    private ScheduledExecutorService poolInspector;

    /** 销毁标记：销毁后巡检线程不再补建容器 */
    private volatile boolean closed = false;

    public DockerSandBoxPool(DockerClient dockerClient,
                             String sandboxImage,
                             String volumeDir, Long memoryLimit,
                             Long memorySwapLimit, Long cpuLimit,
                             int poolSize, String containerNamePrefix) {
        this.dockerClient = dockerClient;
        this.sandboxImage = sandboxImage;
        this.volumeDir = volumeDir;
        this.memoryLimit = memoryLimit;
        this.memorySwapLimit = memorySwapLimit;
        this.cpuLimit = cpuLimit;
        this.poolSize = poolSize;
        this.containerQueue = new ArrayBlockingQueue<>(poolSize);
        this.containerNamePrefix = containerNamePrefix;
        this.containerNameMap = new ConcurrentHashMap<>();
        this.containerUseCount = new ConcurrentHashMap<>();
        this.leasedContainers = ConcurrentHashMap.newKeySet();
    }

    public void initDockerPool() {
        if (poolInspector != null) {
            log.warn("判题沙箱容器池已初始化，忽略重复调用");
            return;
        }
        log.info("------  创建容器开始  -----");
        // 先清掉上一次运行遗留、且超出了当前池大小的容器（比如把 poolSize 从 8 调到 4）
        cleanupOrphanContainers();
        for(int i = 0; i < poolSize; i++) {
            try {
                createContainer(containerNamePrefix + "-" + i);
            } catch (Exception e) {
                log.error("创建第{}个沙箱容器失败", i, e);
            }
        }
        int available = containerQueue.size();
        log.info("------  创建容器结束，可用容器数：{}/{}  -----", available, poolSize);
        // 启动校验：容器池没有建满，说明 Docker 不可用、镜像拉取失败或容器名冲突，
        // 必须让应用启动失败，而不是带着一个永远取不到容器的池子静默运行。
        if (available < poolSize) {
            throw new IllegalStateException("判题沙箱初始化失败：期望 " + poolSize
                    + " 个容器，实际只有 " + available
                    + " 个，请检查 Docker 服务、判题镜像和容器名冲突");
        }
        // 启动巡检：容器在运行期被 OOM-kill 或退出后，由巡检负责补回池子
        startPoolInspector();
    }

    public String getContainer() {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(BORROW_TIMEOUT_SECONDS);
        while (true) {
            long remain = deadline - System.currentTimeMillis();
            if (remain <= 0) {
                throw borrowTimeoutException();
            }
            String containerId;
            try {
                // 带超时的 poll，池子为空时快速失败，避免无限期阻塞判题线程
                containerId = containerQueue.poll(remain, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // 恢复中断标记，交由上层感知线程被中断
                Thread.currentThread().interrupt();
                throw new IllegalStateException("获取判题沙箱容器时线程被中断", e);
            }
            if (containerId == null) {
                throw borrowTimeoutException();
            }
            String containerName = containerNameMap.get(containerId);
            if (containerName == null) {
                log.error("容器 {} 没有对应的容器名，丢弃", containerId);
                continue;
            }
            // 借出前做一次健康检查：容器可能在空闲期间被 OOM-kill 或异常退出
            if (!isContainerRunning(containerId)) {
                log.warn("沙箱容器 {} 借出前检查到已停止，重建后重新借出", containerName);
                recreateContainerSafely(containerName, containerId);
                continue;
            }
            leasedContainers.add(containerId);
            return containerId;
        }
    }

    private IllegalStateException borrowTimeoutException() {
        return new IllegalStateException("获取判题沙箱容器超时（" + BORROW_TIMEOUT_SECONDS
                + " 秒），沙箱池中没有空闲容器，请稍后重试");
    }

    /**
     * 归还容器：必须由借用者调用，且只能调用一次。
     * 归还前调用方应已清理容器内的残留进程和用户代码文件。
     */
    public void returnContainer(String containerId) {
        if (containerId == null) {
            return;
        }
        // 幂等校验：不是本池借出的、或已经归还过的容器直接忽略，避免同一个容器被同时发给两个判题线程
        if (!leasedContainers.remove(containerId)) {
            log.error("容器 {} 不是本池借出的或已被重复归还，忽略本次归还", containerId);
            return;
        }
        String containerName = containerNameMap.get(containerId);
        if (containerName == null) {
            log.error("容器 {} 没有对应的容器名，无法归还，丢弃该容器", containerId);
            return;
        }
        // 归还前做一次健康检查：死容器不能放回队列，否则下一个借到的人必然失败
        if (!isContainerRunning(containerId)) {
            log.warn("沙箱容器 {} 归还时已停止，重建", containerName);
            recreateContainerSafely(containerName, containerId);
            return;
        }
        int used = containerUseCount.merge(containerId, 1, Integer::sum);
        if (used >= RECYCLE_AFTER_USES) {
            log.info("沙箱容器 {} 已使用 {} 次，重建以清理累计状态", containerName, used);
            recreateContainerSafely(containerName, containerId);
            return;
        }
        if (!containerQueue.offer(containerId)) {
            // 队列满说明归还次数异常（重复归还已被上面的幂等校验拦住），这里只记录，不能抛异常打断判题流程
            log.error("沙箱容器队列已满，无法归还容器 {}（{}）", containerName, containerId);
        }
    }

    /**
     * 重建容器：清掉 cgroup 累计内存峰值和容器内累积的 exec 记录，再把新容器放回队列。
     */
    private void recreateContainer(String containerName, String containerId) {
        containerNameMap.remove(containerId);
        containerUseCount.remove(containerId);
        removeContainerQuietly(containerId);
        createContainer(containerName);
    }

    private void recreateContainerSafely(String containerName, String containerId) {
        try {
            recreateContainer(containerName, containerId);
        } catch (Exception e) {
            // 重建失败时该槽位暂时不可用，巡检会在一分钟内补回来
            log.error("重建沙箱容器 {} 失败，等待巡检补建", containerName, e);
        }
    }

    private void removeContainerQuietly(String containerId) {
        try {
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        } catch (Exception e) {
            log.warn("删除沙箱容器 {} 失败：{}", containerId, e.getMessage());
        }
    }

    /**
     * 健康检查：容器是否仍处于运行状态。查不到（容器已被删除）也视为不健康。
     */
    private boolean isContainerRunning(String containerId) {
        try {
            InspectContainerResponse inspectResponse = dockerClient.inspectContainerCmd(containerId).exec();
            return inspectResponse.getState() != null
                    && Boolean.TRUE.equals(inspectResponse.getState().getRunning());
        } catch (Exception e) {
            log.warn("查询沙箱容器 {} 状态失败：{}", containerId, e.getMessage());
            return false;
        }
    }

    /**
     * 启动时清理遗留容器：只处理「前缀 + 数字」且编号 &gt;= poolSize 的容器，
     * 也就是上次以更大 poolSize 运行时留下的孤儿容器。
     */
    private void cleanupOrphanContainers() {
        try {
            List<Container> containerList = dockerClient.listContainersCmd().withShowAll(true).exec();
            if (CollectionUtil.isEmpty(containerList)) {
                return;
            }
            String namePrefix = containerNamePrefix + "-";
            for (Container container : containerList) {
                String[] containerNames = container.getNames();
                if (containerNames == null) {
                    continue;
                }
                for (String nameInDocker : containerNames) {
                    String actualName = nameInDocker.startsWith("/") ? nameInDocker.substring(1) : nameInDocker;
                    if (!actualName.startsWith(namePrefix)) {
                        continue;
                    }
                    Integer index = parseContainerIndex(actualName.substring(namePrefix.length()));
                    if (index == null || index < poolSize) {
                        continue;
                    }
                    log.warn("清理超出池大小的历史沙箱容器：{}（{}）", actualName, container.getId());
                    removeContainerQuietly(container.getId());
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("清理历史沙箱容器失败：{}", e.getMessage());
        }
    }

    private Integer parseContainerIndex(String value) {
        if (value.isEmpty() || value.length() > 9) {
            return null;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return null;
            }
        }
        return Integer.parseInt(value);
    }

    private void startPoolInspector() {
        poolInspector = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "sandbox-pool-inspector");
            thread.setDaemon(true);
            return thread;
        });
        poolInspector.scheduleWithFixedDelay(this::refillPoolSafely,
                INSPECT_INTERVAL_SECONDS, INSPECT_INTERVAL_SECONDS, TimeUnit.SECONDS);
        log.info("沙箱池巡检已启动，间隔 {} 秒", INSPECT_INTERVAL_SECONDS);
    }

    private void refillPoolSafely() {
        if (closed) {
            return;
        }
        try {
            refillPool();
        } catch (Exception e) {
            log.error("沙箱池巡检失败", e);
        }
    }

    /**
     * 补建缺失的容器：池中容器总数（队列中 + 已借出）少于 poolSize 时，把缺的容器补回来。
     */
    private void refillPool() {
        Set<String> activeNames = new HashSet<>();
        for (String containerId : containerQueue) {
            String containerName = containerNameMap.get(containerId);
            if (containerName != null) {
                activeNames.add(containerName);
            }
        }
        for (String containerId : leasedContainers) {
            String containerName = containerNameMap.get(containerId);
            if (containerName != null) {
                activeNames.add(containerName);
            }
        }
        for (int i = 0; i < poolSize; i++) {
            if (closed) {
                return;
            }
            String containerName = containerNamePrefix + "-" + i;
            if (activeNames.contains(containerName)) {
                continue;
            }
            log.warn("巡检发现沙箱容器 {} 缺失，补建", containerName);
            try {
                createContainer(containerName);
            } catch (Exception e) {
                log.error("巡检补建沙箱容器 {} 失败", containerName, e);
            }
        }
    }

    /**
     * 优雅关闭：停止巡检线程并删除本池创建的所有容器，避免进程重启后容器堆积。
     */
    public void destroy() {
        closed = true;
        ScheduledExecutorService inspector = poolInspector;
        if (inspector != null) {
            inspector.shutdownNow();
        }
        synchronized (createLock) {
            Set<String> containerIds = new LinkedHashSet<>(containerNameMap.keySet());
            containerIds.addAll(containerQueue);
            containerIds.addAll(leasedContainers);
            for (String containerId : containerIds) {
                removeContainerQuietly(containerId);
            }
            containerQueue.clear();
            containerNameMap.clear();
            containerUseCount.clear();
            leasedContainers.clear();
            log.info("判题沙箱容器池已销毁，清理容器 {} 个", containerIds.size());
        }
    }

    private void createContainer(String containerName) {
        // 串行化容器创建：巡检线程和判题线程可能同时补建同名容器
        synchronized (createLock) {
            List<Container> containerList = dockerClient.listContainersCmd().withShowAll(true).exec();
            if (!CollectionUtil.isEmpty(containerList)) {
                for (Container container : containerList) {
                    String[] containerNames = container.getNames();
                    if (containerNames == null || containerNames.length == 0) {
                        continue;
                    }
                    for (String nameInDocker : containerNames) {
                        // 去掉 Docker 自动添加的 / 前缀再匹配
                        String actualName = nameInDocker.startsWith("/") ? nameInDocker.substring(1) : nameInDocker;
                        // 必须直接和 containerName 比较：创建容器时用的是不带前缀的 containerName，
                        // 若在此处再拼 JAVA_CONTAINER_PREFIX（"/"），两边永远不相等，复用逻辑失效，
                        // 第二次启动会因同名容器冲突导致全部容器创建失败。
                        if (containerName.equals(actualName)) {
                            log.info("沙箱容器已存在，直接复用：{}（{}）", containerName, container.getId());
                            // 容器已存在，启动后加入队列
                            if ("created".equals(container.getState()) || "exited".equals(container.getState())) {
                                dockerClient.startContainerCmd(container.getId()).exec();
                            }
                            // 防止同一个容器被重复登记（巡检与借出并发时的极小窗口）
                            if (containerQueue.contains(container.getId())
                                    || leasedContainers.contains(container.getId())) {
                                log.debug("容器 {}（{}）已在池中，跳过重复登记", containerName, container.getId());
                            } else {
                                containerQueue.add(container.getId());
                                containerNameMap.put(container.getId(), containerName);
                            }
                            return;
                        }
                    }
                }
            }

            //拉取镜像
            pullJavaEnvImage();
            //创建容器  限制资源   控制权限
            HostConfig hostConfig = getHostConfig(containerName);
            CreateContainerCmd containerCmd = dockerClient
                    .createContainerCmd(JudgeConstants.JAVA_ENV_IMAGE)
                    .withName(containerName);
            CreateContainerResponse createContainerResponse = containerCmd
                    .withHostConfig(hostConfig)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withTty(true)
                    .exec();
            //记录容器id
            String containerId = createContainerResponse.getId();
            //启动容器
            dockerClient.startContainerCmd(containerId).exec();
            containerQueue.add(containerId);
            containerNameMap.put(containerId, containerName);
        }
    }

    private void pullJavaEnvImage() {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();
        for (Image image : imageList) {
            String[] repoTags = image.getRepoTags();
            if (repoTags != null && repoTags.length > 0 && sandboxImage.equals(repoTags[0])) {
                return;
            }
        }
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(sandboxImage);
        try {
            pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //限制资源   控制权限
    private HostConfig getHostConfig(String containerName) {
        HostConfig hostConfig = new HostConfig();
        //设置挂载目录，指定用户代码路径
        String userCodeDir = createContainerDir(containerName);
        hostConfig.setBinds(new Bind(userCodeDir, new Volume(volumeDir)));
        //限制docker容器使用资源
        hostConfig.withMemory(memoryLimit);
        hostConfig.withMemorySwap(memorySwapLimit);
        hostConfig.withCpuCount(cpuLimit);
        hostConfig.withNetworkMode("none");  //禁用网络
        hostConfig.withReadonlyRootfs(true); //禁止在root目录写文件
        return hostConfig;
    }

    public String getCodeDir(String containerId) {
        String containerName = containerNameMap.get(containerId);
        log.info("containerName：{}", containerName);
        return System.getProperty("user.dir") + File.separator + JudgeConstants.CODE_DIR_POOL + File.separator + containerName;
    }

    //为每个容器，创建的指定挂载文件
    private String createContainerDir(String containerName) {
        //一级目录  存放所有容器的挂载目录
        String codeDir = System.getProperty("user.dir") + File.separator + JudgeConstants.CODE_DIR_POOL;
        if (!FileUtil.exist(codeDir)) {
            FileUtil.mkdir(codeDir);
        }
        return codeDir + File.separator + containerName;
    }
}

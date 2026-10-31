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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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
        this.containerNameMap = new HashMap<>();
    }

    public void initDockerPool() {
        log.info("------  创建容器开始  -----");
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
    }

    public String getContainer() {
        try {
            // 使用带超时的 poll，池子为空时快速失败，避免无限期阻塞判题线程
            String containerId = containerQueue.poll(BORROW_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (containerId == null) {
                throw new IllegalStateException("获取判题沙箱容器超时（" + BORROW_TIMEOUT_SECONDS
                        + " 秒），沙箱池中没有空闲容器，请稍后重试");
            }
            return containerId;
        } catch (InterruptedException e) {
            // 恢复中断标记，交由上层感知线程被中断
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取判题沙箱容器时线程被中断", e);
        }
    }

    public void returnContainer(String containerId) {
        containerQueue.add(containerId);
    }

    private void createContainer(String containerName) {
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
                        containerQueue.add(container.getId());
                        containerNameMap.put(container.getId(), containerName);
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

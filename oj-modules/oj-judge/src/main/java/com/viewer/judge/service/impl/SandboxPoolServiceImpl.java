package com.viewer.judge.service.impl;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.viewer.common.core.constants.Constants;
import com.viewer.common.core.constants.JudgeConstants;
import com.viewer.common.core.enums.CodeRunStatus;
import com.viewer.judge.callback.DockerStartResultCallback;
import com.viewer.judge.callback.StatisticsCallback;
import com.viewer.judge.config.DockerSandBoxPool;
import com.viewer.judge.domain.CompileResult;
import com.viewer.judge.domain.SandBoxExecuteResult;
import com.viewer.judge.service.ISandboxPoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Docker 容器池的 Java 沙箱实现。
 *
 * <p>并发安全：容器 id、代码目录、代码文件名都必须是方法内的局部变量。
 * 之前的实现把它们放在单例字段里，一旦判题并发（MQ 消费者并发 &gt; 1），
 * 两个请求会互相覆盖对方的容器和文件路径，导致代码写错目录、结果串题。</p>
 *
 * <p>资源清理：一次判题 = 借出容器 → 执行 → 清理 → 归还。
 * 清理必须在 finally 里完成，否则超时进程、stats 流、编译产物都会留给下一个用户。</p>
 */
@Service
@Slf4j
public class SandboxPoolServiceImpl implements ISandboxPoolService {

    /** 编译超时（秒）：javac 卡住时不能永久占用一个容器 */
    private static final long COMPILE_TIMEOUT_SECONDS = 30L;

    /** 清理残留进程的超时（秒） */
    private static final long CLEANUP_TIMEOUT_SECONDS = 5L;

    /**
     * 杀掉容器内残留的 java/javac 进程（超时的用户代码、被中断的编译）。
     * 注意两点：
     * 1. 必须排除 PID 1：openjdk 镜像的默认 CMD 是 jshell，本身就是一个 java 进程，
     *    误杀 PID 1 会让整个容器退出；
     * 2. 不依赖 pkill/pgrep 是否存在于镜像里，直接用 /proc 判断进程名，缺失时只是杀不掉、不会出错。
     */
    private static final String[] KILL_RESIDUAL_PROCESS_CMD = new String[]{
            "sh", "-c",
            "for d in /proc/[0-9]*; do "
                    + "p=${d#/proc/}; "
                    + "if [ \"$p\" != \"1\" ]; then "
                    + "c=$(cat $d/comm 2>/dev/null); "
                    + "if [ \"$c\" = \"java\" ] || [ \"$c\" = \"javac\" ]; then kill -9 $p; fi; "
                    + "fi; "
                    + "done; true"};

    @Autowired
    private DockerSandBoxPool sandBoxPool;

    @Autowired
    private DockerClient dockerClient;

    @Value("${sandbox.limit.time:5}")
    private Long timeLimit;

    @Override
    public SandBoxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList) {
        // 容器与文件路径都是本次请求的局部变量，避免并发请求互相覆盖
        String containerId;
        try {
            containerId = sandBoxPool.getContainer();
        } catch (IllegalStateException e) {
            // 池子忙/不可用：返回业务失败而不是把异常抛给 MQ 监听器，
            // 否则默认的 requeue 行为会让这条消息被无限重投（且阻塞唯一的消费者）
            log.error("获取判题沙箱容器失败：{}", e.getMessage());
            return SandBoxExecuteResult.fail(CodeRunStatus.UNKNOWN_FAILED, "判题服务繁忙，请稍后重新提交");
        }
        String codeDir = null;
        try {
            codeDir = sandBoxPool.getCodeDir(containerId);
            createUserCodeFile(codeDir, userCode);
            //编译代码
            CompileResult compileResult = compileCodeByDocker(containerId);
            if (!compileResult.isCompiled()) {
                return SandBoxExecuteResult.fail(CodeRunStatus.COMPILE_FAILED, compileResult.getExeMessage());
            }
            //执行代码
            return executeJavaCodeByDocker(containerId, inputList);
        } finally {
            // 归还前必须清理：杀掉超时/残留的用户进程，删掉编译产物，再把容器放回池子，
            // 否则下一个借到该容器的用户会看到上一个用户的进程和文件。
            killResidualJavaProcesses(containerId);
            cleanCodeDir(codeDir);
            sandBoxPool.returnContainer(containerId);
        }
    }

    //创建用户代码文件
    private void createUserCodeFile(String codeDir, String userCode) {
        String userCodeFileName = codeDir + File.separator + JudgeConstants.USER_CODE_JAVA_CLASS_NAME;
        // 容器是复用的，目录里可能残留上一个用户的文件，先删掉
        FileUtil.del(userCodeFileName);
        FileUtil.writeString(userCode, userCodeFileName, Constants.UTF8);
        log.info("用户代码写入：{}", userCodeFileName);
    }

    //编译
    //的使用docker编译
    private CompileResult compileCodeByDocker(String containerId) {
        CompileResult compileResult = new CompileResult();
        DockerStartResultCallback resultCallback = new DockerStartResultCallback();
        try {
            String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVAC_CMD, null, containerId);
            boolean finished = dockerClient.execStartCmd(cmdId)
                    .exec(resultCallback)
                    // 编译也要有超时，避免 javac 卡死时长期占用容器
                    .awaitCompletion(COMPILE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                compileResult.setCompiled(false);
                compileResult.setExeMessage("编译超时");
                return compileResult;
            }
            if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                compileResult.setCompiled(false);
                compileResult.setExeMessage(resultCallback.getErrorMessage());
            } else {
                compileResult.setCompiled(true);
            }
            return compileResult;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("编译用户代码时线程被中断", e);
        }
    }

    private SandBoxExecuteResult executeJavaCodeByDocker(String containerId, List<String> inputList) {
        List<String> outList = new ArrayList<>(); //记录输出结果
        long maxMemory = 0L;  //最大占用内存
        long maxUseTime = 0L; //最大运行时间
        //执行用户代码
        for (String inputArgs : inputList) {
            String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArgs, containerId);
            //执行情况监控
            StatsCmd statsCmd = dockerClient.statsCmd(containerId); //启动监控
            StatisticsCallback statisticsCallback = statsCmd.exec(new StatisticsCallback());
            DockerStartResultCallback resultCallback = new DockerStartResultCallback();
            StopWatch stopWatch = new StopWatch();        //执行代码后开始计时
            boolean finished;
            stopWatch.start();
            try {
                // 超时后只是不再等待，容器里的 java 进程仍会继续运行，下面要主动杀掉
                finished = dockerClient.execStartCmd(cmdId)
                        .exec(resultCallback)
                        .awaitCompletion(timeLimit, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("执行用户代码时线程被中断", e);
            } finally {
                // stats 流必须在 finally 里关闭，否则用例失败提前返回时会泄漏连接和线程
                stopWatch.stop();
                statsCmd.close();
            }
            long userTime = stopWatch.getLastTaskTimeMillis(); //执行耗时
            maxUseTime = Math.max(userTime, maxUseTime);       //记录最大的执行用例耗时
            Long memory = statisticsCallback.getMaxMemory();
            if (memory != null) {
                maxMemory = Math.max(memory, maxMemory);       //记录最大的执行用例占用内存
            }
            if (!finished) {
                // 超时：立即杀掉容器内还在跑的 java 进程，避免它继续消耗 CPU/内存、污染后续判题
                log.warn("用户代码执行超过 {} 秒，终止该用例并清理残留进程", timeLimit);
                killResidualJavaProcesses(containerId);
                return SandBoxExecuteResult.fail(CodeRunStatus.OUT_OF_TIME, outList, maxMemory, maxUseTime);
            }
            if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                //未通过所有用例返回结果，同时带上程序写到 stderr 的异常栈，方便定位问题
                return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED, resultCallback.getErrorMessage(),
                        outList, maxMemory, maxUseTime);
            }
            String message = resultCallback.getMessage();
            outList.add(message == null ? "" : message.trim());   //记录正确的输出结果
        }
        if (inputList.size() != outList.size()) {
            //输入用例数量 不等于 输出用例数量  属于执行异常
            return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED, outList, maxMemory, maxUseTime);
        }
        return SandBoxExecuteResult.success(CodeRunStatus.SUCCEED, outList, maxMemory, maxUseTime);
    }

    private String createExecCmd(String[] javaCmdArr, String inputArgs, String containerId) {
        if (!StrUtil.isEmpty(inputArgs)) {
            //当入参不为空时拼接入参
            String[] inputArray = inputArgs.split(" "); //入参
            javaCmdArr = ArrayUtil.append(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArray);
        }
        ExecCreateCmdResponse cmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(javaCmdArr)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        return cmdResponse.getId();
    }

    /**
     * 执行清理命令，杀掉容器内残留的用户 java/javac 进程。
     * 清理失败不能影响判题结果和容器归还，因此这里吞掉异常只记日志。
     */
    private void killResidualJavaProcesses(String containerId) {
        if (StrUtil.isEmpty(containerId)) {
            return;
        }
        try {
            ExecCreateCmdResponse cmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(KILL_RESIDUAL_PROCESS_CMD)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            dockerClient.execStartCmd(cmdResponse.getId())
                    .exec(new DockerStartResultCallback())
                    .awaitCompletion(CLEANUP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            // 清理失败不能影响判题结果和容器归还，只记录
            log.warn("清理容器 {} 内残留的用户进程失败：{}", containerId, e.getMessage());
        }
    }

    /**
     * 清空挂载目录里的编译产物（Solution.java、Solution.class 以及用户代码自己写的文件），
     * 保留目录本身，因为容器启动时 bind mount 的就是这个目录。
     */
    private void cleanCodeDir(String codeDir) {
        if (StrUtil.isEmpty(codeDir)) {
            return;
        }
        File dir = new File(codeDir);
        if (!dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                FileUtil.del(file);
            } catch (Exception e) {
                log.warn("清理沙箱文件 {} 失败：{}", file.getAbsolutePath(), e.getMessage());
            }
        }
    }
}

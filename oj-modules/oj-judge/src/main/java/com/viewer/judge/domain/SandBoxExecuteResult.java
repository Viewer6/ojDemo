package com.viewer.judge.domain;

import cn.hutool.core.util.StrUtil;
import com.viewer.common.core.enums.CodeRunStatus;
import lombok.Data;

import java.util.List;

/**
 * 沙箱执行结果
 */
@Data
public class SandBoxExecuteResult {

    /**
     * 执行状态
     */
    private CodeRunStatus runStatus;

    /**
     * 异常信息
     */
    private String exeMessage;

    /**
     * 每个用例的实际输出
     */
    private List<String> outputList;

    /**
     * 最大占用内存（字节）
     */
    private Long useMemory;

    /**
     * 最大耗时（毫秒）
     */
    private Long useTime;

    public static SandBoxExecuteResult fail(CodeRunStatus runStatus, String errorMsg) {
        SandBoxExecuteResult result = new SandBoxExecuteResult();
        result.setRunStatus(runStatus);
        result.setExeMessage(errorMsg);
        return result;
    }

    public static SandBoxExecuteResult fail(CodeRunStatus runStatus) {
        SandBoxExecuteResult result = new SandBoxExecuteResult();
        result.setRunStatus(runStatus);
        result.setExeMessage(runStatus.getMsg());
        return result;
    }

    public static SandBoxExecuteResult fail(CodeRunStatus runStatus, List<String> outputList,
                                            Long useMemory, Long useTime) {
        SandBoxExecuteResult result = new SandBoxExecuteResult();
        result.setRunStatus(runStatus);
        result.setExeMessage(runStatus.getMsg());
        result.setOutputList(outputList);
        result.setUseMemory(useMemory);
        result.setUseTime(useTime);
        return result;
    }

    /**
     * 失败时带上沙箱里的原始报错（编译错误、异常栈等），而不是只返回状态枚举的文案。
     * 报错为空时回退到状态默认文案，避免把空串写进库。
     */
    public static SandBoxExecuteResult fail(CodeRunStatus runStatus, String exeMessage,
                                            List<String> outputList, Long useMemory, Long useTime) {
        SandBoxExecuteResult result = fail(runStatus, outputList, useMemory, useTime);
        result.setExeMessage(StrUtil.isBlank(exeMessage) ? runStatus.getMsg() : exeMessage);
        return result;
    }

    public static SandBoxExecuteResult success(CodeRunStatus runStatus, List<String> outputList,
                                               Long useMemory, Long useTime) {
        SandBoxExecuteResult result = new SandBoxExecuteResult();
        result.setRunStatus(runStatus);
        result.setOutputList(outputList);
        result.setUseMemory(useMemory);
        result.setUseTime(useTime);
        return result;
    }
}

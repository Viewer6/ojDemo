package com.viewer.judge.domain;

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

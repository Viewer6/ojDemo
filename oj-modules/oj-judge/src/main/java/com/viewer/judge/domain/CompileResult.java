package com.viewer.judge.domain;

import lombok.Data;

/**
 * 编译结果
 */
@Data
public class CompileResult {

    /**
     * 是否编译成功
     */
    private boolean compiled;

    /**
     * 编译输出信息（失败时为错误信息）
     */
    private String exeMessage;
}

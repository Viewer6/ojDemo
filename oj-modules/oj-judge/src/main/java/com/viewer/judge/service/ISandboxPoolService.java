package com.viewer.judge.service;

import com.viewer.judge.domain.SandBoxExecuteResult;

import java.util.List;

/**
 * Docker 沙箱执行服务
 */
public interface ISandboxPoolService {

    /**
     * 编译并执行 Java 代码
     *
     * @param userId    用户 id
     * @param userCode  用户提交的 Solution 代码
     * @param mainFuc   题目配置的 Main 类模板
     * @param inputList 每个用例的入参
     * @return 执行结果
     */
    SandBoxExecuteResult exeJavaCode(Long userId, String userCode, String mainFuc, List<String> inputList);
}

package com.viewer.judge.service;

import org.example.api.domain.dto.JudgeSubmitDTO;
import org.example.api.domain.vo.UserQuestionResultVO;

/**
 * 判题服务
 */
public interface IJudgeService {

    /**
     * 判 Java 代码
     *
     * @param judgeSubmitDTO 判题参数
     * @return 判题结果
     */
    UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO);
}

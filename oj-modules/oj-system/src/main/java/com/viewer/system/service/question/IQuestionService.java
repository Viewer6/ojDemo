package com.viewer.system.service.question;

import com.viewer.common.core.domain.Result;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.system.domain.question.dto.QuestionAddDTO;
import com.viewer.system.domain.question.dto.QuestionListDTO;
import com.viewer.system.domain.question.vo.QuestionListVO;

import java.util.List;

public interface IQuestionService {
    List<QuestionListVO> getList(QuestionListDTO questionListDTO);

    int add(QuestionAddDTO questionAddDTO);
}

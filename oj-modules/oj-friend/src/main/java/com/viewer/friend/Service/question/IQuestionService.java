package com.viewer.friend.Service.question;

import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.friend.domain.question.dto.QuestionQueryDTO;
import com.viewer.friend.domain.question.vo.QuestionDetailVO;
import com.viewer.friend.domain.question.vo.QuestionVO;

import java.util.List;

public interface IQuestionService {
    TableDataInfo list(QuestionQueryDTO questionQueryDTO);

    List<QuestionVO> hotList();

    QuestionDetailVO detail(Long questionId);

    String preQuestion(Long questionId);

    String nextQuestion(Long questionId);

}

package com.viewer.system.mapper.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viewer.system.domain.exam.ExamQuestion;
import com.viewer.system.domain.question.vo.QuestionListVO;

import java.util.List;

public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {
    List<QuestionListVO> selectExamQuestionList(Long examId);
}

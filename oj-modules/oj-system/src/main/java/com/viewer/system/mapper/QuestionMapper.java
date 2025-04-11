package com.viewer.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viewer.system.domain.question.Question;
import com.viewer.system.domain.question.dto.QuestionListDTO;
import com.viewer.system.domain.question.vo.QuestionListVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QuestionMapper extends BaseMapper<Question> {
    List<QuestionListVO> selectQuestionList(QuestionListDTO questionListDTO);
}


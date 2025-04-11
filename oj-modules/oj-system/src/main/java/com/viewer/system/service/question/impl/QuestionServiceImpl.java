package com.viewer.system.service.question.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.system.domain.question.dto.QuestionListDTO;
import com.viewer.system.domain.question.vo.QuestionListVO;
import com.viewer.system.mapper.QuestionMapper;
import com.viewer.system.service.question.IQuestionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements IQuestionService {

    @Resource(name = "questionMapper")
    private QuestionMapper questionMapper;
    @Override
    public List<QuestionListVO> getList(QuestionListDTO questionListDTO) {
        PageHelper.startPage(questionListDTO.getPageNum(), questionListDTO.getPageSize());
        return questionMapper.selectQuestionList(questionListDTO);
    }
}

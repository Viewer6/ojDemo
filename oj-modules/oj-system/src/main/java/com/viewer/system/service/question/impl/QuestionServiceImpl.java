package com.viewer.system.service.question.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.common.core.emuns.ResultCode;
import com.viewer.common.core.exception.QuestionException;
import com.viewer.system.domain.question.Question;
import com.viewer.system.domain.question.dto.QuestionAddDTO;
import com.viewer.system.domain.question.dto.QuestionListDTO;
import com.viewer.system.domain.question.vo.QuestionListVO;
import com.viewer.system.mapper.QuestionMapper;
import com.viewer.system.service.question.IQuestionService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Override
    public int add(QuestionAddDTO questionAddDTO) {
        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getTitle, questionAddDTO.getTitle())
        );
        if(!CollectionUtil.isEmpty(questions)){
            throw new QuestionException(ResultCode.AILED_QUESTION_EXISTS);
        }

        // 将输入转化
        Question question = new Question();
        BeanUtils.copyProperties(questionAddDTO, question);
        return questionMapper.insert(question);
    }
}

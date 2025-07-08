package com.viewer.system.service.question.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.viewer.common.core.constants.Constants;
import com.viewer.common.core.emuns.ResultCode;
import com.viewer.common.core.exception.QuestionException;
import com.viewer.system.domain.question.Question;
import com.viewer.system.domain.question.dto.QuestionAddDTO;
import com.viewer.system.domain.question.dto.QuestionEditDTO;
import com.viewer.system.domain.question.dto.QuestionListDTO;
import com.viewer.system.domain.question.vo.QuestionDetailVO;
import com.viewer.system.domain.question.vo.QuestionListVO;
import com.viewer.system.mapper.question.QuestionMapper;
import com.viewer.system.service.question.IQuestionService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements IQuestionService {

    @Resource(name = "questionMapper")
    private QuestionMapper questionMapper;
    @Override
    public List<QuestionListVO> getList(QuestionListDTO questionListDTO) {
        String filterQuestionIdsStr = questionListDTO.getFilterQuestionIdsStr();
        if(StrUtil.isNotEmpty(filterQuestionIdsStr)){
            String[] split = filterQuestionIdsStr.split(Constants.SPLIT_SMC);
            // 使用stream流对数据进行转换
            Set<Long> filterQuestionIds = Arrays.stream(split)
                    .map(Long::valueOf)
                    .collect(Collectors.toSet());

            questionListDTO.setFilterQuestionIds(filterQuestionIds);
        }

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

    @Override
    public QuestionDetailVO getDetail(Long queryQuestionId) {
        Question question = questionMapper.selectOne(new LambdaQueryWrapper<Question>()
                .eq(Question::getQuestionId, queryQuestionId)
        );
        if (question == null){
            throw new QuestionException(ResultCode.FAILED_QUESTION_NOT_EXISTS);
        }

        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        BeanUtils.copyProperties(question, questionDetailVO);
        return questionDetailVO;
    }

    @Override
    public int edit(QuestionEditDTO questionEditDTO) {
        Question oldQuestion = questionMapper.selectById(questionEditDTO.getQuestionId());
        if(oldQuestion == null){
            throw new QuestionException(ResultCode.FAILED_QUESTION_NOT_EXISTS);
        }

        oldQuestion.setTitle(questionEditDTO.getTitle());
        oldQuestion.setDifficulty(questionEditDTO.getDifficulty());
        oldQuestion.setTimeLimit(questionEditDTO.getTimeLimit());
        oldQuestion.setSpaceLimit(questionEditDTO.getSpaceLimit());
        oldQuestion.setContent(questionEditDTO.getContent());
        oldQuestion.setQuestionCase(questionEditDTO.getQuestionCase());
        oldQuestion.setDefaultCode(questionEditDTO.getDefaultCode());
        oldQuestion.setMainFuc(questionEditDTO.getMainFuc());

        Question question = new Question();
        BeanUtils.copyProperties(oldQuestion, question);
        return questionMapper.updateById(question);
    }

    @Override
    public int delete(Long deleteQuestionId) {
        Question question = questionMapper.selectById(deleteQuestionId);
        if(question == null){
            throw new QuestionException(ResultCode.FAILED_QUESTION_NOT_EXISTS);
        }

        return questionMapper.deleteById(deleteQuestionId);
    }
}

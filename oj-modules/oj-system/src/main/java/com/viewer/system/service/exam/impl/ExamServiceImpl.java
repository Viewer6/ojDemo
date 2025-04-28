package com.viewer.system.service.exam.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.viewer.common.core.emuns.ResultCode;
import com.viewer.common.core.exception.ExamException;
import com.viewer.common.core.exception.QuestionException;
import com.viewer.system.domain.exam.Exam;
import com.viewer.system.domain.exam.ExamQuestion;
import com.viewer.system.domain.exam.dto.ExamAddDTO;
import com.viewer.system.domain.exam.dto.ExamQueryDTO;
import com.viewer.system.domain.exam.dto.ExamQuestionAddDTO;
import com.viewer.system.domain.exam.vo.ExamListVO;
import com.viewer.system.domain.question.Question;
import com.viewer.system.mapper.exam.ExamMapper;
import com.viewer.system.mapper.exam.ExamQuestionMapper;
import com.viewer.system.mapper.question.QuestionMapper;
import com.viewer.system.service.exam.IExamService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ExamServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements IExamService {

    @Resource(name = "examMapper")
    private ExamMapper examMapper;

    @Resource(name = "questionMapper")
    private QuestionMapper questionMapper;

    @Resource(name = "examQuestionMapper")
    private ExamQuestionMapper examQuestionMapper;

    @Override
    public List<ExamListVO> getList(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public String add(ExamAddDTO examAddDTO) {
        List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getTitle, examAddDTO.getTitle()));
        if(CollectionUtil.isNotEmpty(exams)){
            throw new ExamException(ResultCode.EXAM_ALREADY_EXISTS);
        }
        if(examAddDTO.getStartTime().isBefore(LocalDateTime.now())){
            throw new ExamException(ResultCode.EXAM_START_TIME_BEFORE_CURRENT_TIME);
        }
        if(examAddDTO.getStartTime().isAfter(examAddDTO.getEndTime())){
            throw new ExamException(ResultCode.EXAM_START_TIME_AFTER_END_TIME);
        }

        Exam exam = new Exam();
        BeanUtils.copyProperties(examAddDTO, exam);

        examMapper.insert(exam);
        return exam.getExamId().toString();
    }

    @Override
    public boolean addQuestion(ExamQuestionAddDTO questionAddDTO) {
        Exam exam = examMapper.selectById(questionAddDTO.getExamId());
        if(exam == null){
            throw new ExamException(ResultCode.EXAM_NOT_EXISTS);
        }

        Set<Long> questionIds = questionAddDTO.getQuestionIdSet();
        if(CollectionUtil.isEmpty(questionIds)){
            return true;
        }
        List<Question> questionList = questionMapper.selectBatchIds(questionIds);

        if (CollectionUtil.isEmpty(questionList) || questionList.size() < questionIds.size()) {
            throw new QuestionException(ResultCode.FAILED_QUESTION_NOT_EXISTS);
        }

        int num = 1; // 题目顺序
        List<ExamQuestion> examQuestionList = new ArrayList<>();
        for (Long questionId : questionIds) {
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamId(exam.getExamId());
            examQuestion.setQuestionId(questionId);
            examQuestion.setQuestionOrder(num++);
            examQuestionList.add(examQuestion);
        }

        return saveBatch(examQuestionList);
    }
}

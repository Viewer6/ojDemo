package com.viewer.system.service.exam.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.viewer.common.core.constants.Constants;
import com.viewer.common.core.emuns.ResultCode;
import com.viewer.common.core.exception.ExamException;
import com.viewer.common.core.exception.QuestionException;
import com.viewer.system.domain.exam.Exam;
import com.viewer.system.domain.exam.ExamQuestion;
import com.viewer.system.domain.exam.dto.ExamAddDTO;
import com.viewer.system.domain.exam.dto.ExamEditDTO;
import com.viewer.system.domain.exam.dto.ExamQueryDTO;
import com.viewer.system.domain.exam.dto.ExamQuestionAddDTO;
import com.viewer.system.domain.exam.vo.ExamDetailVO;
import com.viewer.system.domain.exam.vo.ExamListVO;
import com.viewer.system.domain.question.Question;
import com.viewer.system.domain.question.vo.QuestionListVO;
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
        checkExamTime(examAddDTO, null);

        Exam exam = new Exam();
        BeanUtils.copyProperties(examAddDTO, exam);

        examMapper.insert(exam);
        return exam.getExamId().toString();
    }



    @Override
    public boolean addQuestion(ExamQuestionAddDTO questionAddDTO) {

        checkExamTime(questionAddDTO.getExamId());

        Set<Long> questionIds = questionAddDTO.getQuestionIdSet();
        if(CollectionUtil.isEmpty(questionIds)){
            return true;
        }
        List<Question> questionList = questionMapper.selectBatchIds(questionIds);

        if (CollectionUtil.isEmpty(questionList) || questionList.size() < questionIds.size()) {
            throw new QuestionException(ResultCode.FAILED_QUESTION_NOT_EXISTS);
        }

        List<ExamQuestion> examQuestionList = getExamQuestionList(questionAddDTO.getExamId(), questionIds);

        return saveBatch(examQuestionList);
    }

    @Override
    public int deleteQuestion(Long examId, Long questionId) {
        checkExamTime(examId);

        return examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId)
                .eq(ExamQuestion::getQuestionId, questionId));
    }


    @Override
    public ExamDetailVO detail(Long examId) {
        Exam exam = getExam(examId);

        ExamDetailVO examDetailVO = new ExamDetailVO();
        BeanUtils.copyProperties(exam, examDetailVO);

        List<QuestionListVO> questionVOList = examQuestionMapper.selectExamQuestionList(examId);
        if (CollectionUtil.isEmpty(questionVOList)) {
            return examDetailVO;
        }
        examDetailVO.setExamQuestionList(questionVOList);
        return examDetailVO;

    }

    @Override
    public int edit(ExamEditDTO examEditDTO) {
        Exam exam = getExam(examEditDTO.getExamId());

        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ExamException(ResultCode.EXAM_IS_PUBLISH);
        }

        checkExamTime(exam.getExamId());
        checkExamTime(examEditDTO, examEditDTO.getExamId());

        exam.setTitle(examEditDTO.getTitle());
        exam.setStartTime(examEditDTO.getStartTime());
        exam.setEndTime(examEditDTO.getEndTime());
        return examMapper.updateById(exam);
    }

    @Override
    public int delete(Long examId) {
        checkExamTime(examId);

        examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId));

        return examMapper.delete(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getExamId, examId));
    }

    @Override
    public int publish(Long examId) {
        return publishOperation(examId, 1);
    }

    @Override
    public int cancelPublish(Long examId) {
        return publishOperation(examId, 0);
    }

    /**
     * 发布竞赛和撤销发布竞赛
     * @param examId
     * @param status
     * @return
     */
    private int publishOperation(Long examId, Integer status){
        Exam exam = getExam(examId);
        checkExamTime(examId);

        Long count = examQuestionMapper.selectCount(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId));
        if(count == null || count<=0){
            throw new ExamException(ResultCode.EXAM_NOT_HAS_QUESTION);
        }

        exam.setStatus(status);
        return examMapper.updateById(exam);
    }

    /**
     * 规范竞赛题目顺序
     * @param examId 竞赛id
     * @param questionIds 题目列表id
     * @return
     */
    private List<ExamQuestion> getExamQuestionList(Long examId, Set<Long> questionIds){
        int num = 1; // 题目顺序
        List<ExamQuestion> examQuestionList = new ArrayList<>();
        for (Long questionId : questionIds) {
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamId(examId);
            examQuestion.setQuestionId(questionId);
            examQuestion.setQuestionOrder(num++);
            examQuestionList.add(examQuestion);
        }

        return examQuestionList;
    }

    /**
     * 检查竞赛是否存在并获取竞赛
     * @param examId
     * @return
     */
    private Exam getExam(Long examId){
        return checkExamId(examId);
    }
    private Exam checkExamId(Long examId){
        Exam exam = examMapper.selectById(examId);
        if(exam == null){
            throw new ExamException(ResultCode.EXAM_NOT_EXISTS);
        }
        return exam;
    }

    /**
     * 检查修改的竞赛是否已经开赛
     */
    private void checkExamTime(Long examId){
        Exam exam = getExam(examId);
        if(exam.getStartTime().isBefore(LocalDateTime.now())){
            throw new ExamException(ResultCode.EXAM_STARTED);
        }
    }

    /**
     * 检查添加竞赛的时间是否合规
     */
    private void checkExamTime(ExamAddDTO examSaveDTO, Long examId){
        List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getTitle, examSaveDTO.getTitle())
                .ne(examId != null, Exam::getExamId, examId));
        if(CollectionUtil.isNotEmpty(exams)){
            throw new ExamException(ResultCode.EXAM_ALREADY_EXISTS);
        }
        if(examSaveDTO.getStartTime().isBefore(LocalDateTime.now())){
            throw new ExamException(ResultCode.EXAM_START_TIME_BEFORE_CURRENT_TIME);
        }
        if(examSaveDTO.getStartTime().isAfter(examSaveDTO.getEndTime())){
            throw new ExamException(ResultCode.EXAM_START_TIME_AFTER_END_TIME);
        }
    }
}

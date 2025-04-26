package com.viewer.system.service.exam.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.viewer.common.core.emuns.ResultCode;
import com.viewer.common.core.exception.ExamException;
import com.viewer.system.domain.exam.Exam;
import com.viewer.system.domain.exam.dto.ExamAddDTO;
import com.viewer.system.domain.exam.dto.ExamQueryDTO;
import com.viewer.system.domain.exam.vo.ExamListVO;
import com.viewer.system.mapper.exam.ExamMapper;
import com.viewer.system.service.exam.IExamService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamServiceImpl implements IExamService {

    @Resource(name = "examMapper")
    private ExamMapper examMapper;

    @Override
    public List<ExamListVO> getList(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public int add(ExamAddDTO examAddDTO) {
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

        return examMapper.insert(exam);
    }
}

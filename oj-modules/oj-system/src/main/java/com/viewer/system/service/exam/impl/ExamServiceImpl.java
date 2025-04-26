package com.viewer.system.service.exam.impl;

import com.github.pagehelper.PageHelper;
import com.viewer.system.domain.exam.dto.ExamQueryDTO;
import com.viewer.system.domain.exam.vo.ExamListVO;
import com.viewer.system.mapper.exam.ExamMapper;
import com.viewer.system.service.exam.IExamService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
}

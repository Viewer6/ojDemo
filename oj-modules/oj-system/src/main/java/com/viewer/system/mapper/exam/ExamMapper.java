package com.viewer.system.mapper.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viewer.system.domain.exam.Exam;
import com.viewer.system.domain.exam.dto.ExamQueryDTO;
import com.viewer.system.domain.exam.vo.ExamListVO;

import java.util.List;

public interface ExamMapper extends BaseMapper<Exam> {

    List<ExamListVO> selectExamList(ExamQueryDTO examQueryDTO);
}

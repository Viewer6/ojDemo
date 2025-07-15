package com.viewer.friend.mapper.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viewer.friend.domain.exam.Exam;
import com.viewer.friend.domain.exam.dto.ExamQueryDTO;
import com.viewer.friend.domain.exam.vo.ExamListVO;

import java.util.List;

public interface ExamMapper extends BaseMapper<Exam> {
    List<ExamListVO> selectExamList(ExamQueryDTO examQueryDTO);
}

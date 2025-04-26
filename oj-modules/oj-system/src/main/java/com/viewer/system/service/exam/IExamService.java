package com.viewer.system.service.exam;

import com.viewer.system.domain.exam.dto.ExamQueryDTO;
import com.viewer.system.domain.exam.vo.ExamListVO;

import java.util.List;

public interface IExamService {
    List<ExamListVO> getList(ExamQueryDTO examQueryDTO);
}

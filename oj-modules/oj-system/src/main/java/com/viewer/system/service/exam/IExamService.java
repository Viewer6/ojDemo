package com.viewer.system.service.exam;

import com.viewer.system.domain.exam.dto.ExamAddDTO;
import com.viewer.system.domain.exam.dto.ExamQueryDTO;
import com.viewer.system.domain.exam.dto.ExamQuestionAddDTO;
import com.viewer.system.domain.exam.vo.ExamDetailVO;
import com.viewer.system.domain.exam.vo.ExamListVO;

import java.util.List;

public interface IExamService {
    List<ExamListVO> getList(ExamQueryDTO examQueryDTO);

    String add(ExamAddDTO examAddDTO);

    boolean addQuestion(ExamQuestionAddDTO questionAddDTO);

    ExamDetailVO detail(Long examId);
}

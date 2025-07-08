package com.viewer.system.service.exam;

import com.viewer.system.domain.exam.dto.ExamAddDTO;
import com.viewer.system.domain.exam.dto.ExamEditDTO;
import com.viewer.system.domain.exam.dto.ExamQueryDTO;
import com.viewer.system.domain.exam.dto.ExamQuestionAddDTO;
import com.viewer.system.domain.exam.vo.ExamDetailVO;
import com.viewer.system.domain.exam.vo.ExamListVO;
import com.viewer.system.service.exam.impl.ExamServiceImpl;

import java.util.List;

public interface IExamService {
    List<ExamListVO> getList(ExamQueryDTO examQueryDTO);

    String add(ExamAddDTO examAddDTO);

    boolean addQuestion(ExamQuestionAddDTO questionAddDTO);

    int deleteQuestion(Long examId, Long questionId);

    ExamDetailVO detail(Long examId);
    int edit(ExamEditDTO examEditDTO);
    int delete(Long examId);

    int publish(Long examId);

    int cancelPublish(Long examId);
}

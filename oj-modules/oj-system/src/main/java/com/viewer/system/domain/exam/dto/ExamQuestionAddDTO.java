package com.viewer.system.domain.exam.dto;

import lombok.Data;

import java.util.LinkedHashSet;

@Data
public class ExamQuestionAddDTO {

    private Long examId;

    private LinkedHashSet<Long> questionIdSet;
}

package com.viewer.system.domain.question.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionEditDTO {
    private String title;

    private Integer difficulty;

    private Long timeLimit;

    private Long spaceLimit;

    private String content;

    private String questionCase;

    private String defaultCode;

    private String mainFuc;
}

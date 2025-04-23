package com.viewer.system.domain.question.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionEditDTO {
    @NotNull
    private Long queryQuestionId;
}

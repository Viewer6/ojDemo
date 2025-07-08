package com.viewer.system.domain.question.dto;


import com.viewer.common.core.domain.PageQueryDTO;
import lombok.Data;

import java.util.Set;

@Data
public class QuestionListDTO extends PageQueryDTO {
    private Integer difficulty;

    private String title;

    private String filterQuestionIdsStr;

    private Set<Long> filterQuestionIds;
}

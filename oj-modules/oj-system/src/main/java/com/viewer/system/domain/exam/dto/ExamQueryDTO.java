package com.viewer.system.domain.exam.dto;

import com.viewer.common.core.domain.PageQueryDTO;
import lombok.Data;

@Data
public class ExamQueryDTO extends PageQueryDTO {
    private String title;

    private String startTime;

    private String endTime;
}

package com.viewer.friend.domain.exam.dto;

import com.viewer.common.core.domain.PageQueryDTO;
import lombok.Data;

@Data
public class ExamQueryDTO extends PageQueryDTO {
    private String title;

    private String startTime;

    private String endTime;

    private Integer type; // 0- 未开赛  1- 已开赛
}

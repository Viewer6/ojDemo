package com.viewer.system.domain.question.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionVO {
    private Long questionId;

    private String title;

    private Integer difficulty; // 1-简单 2-中等 3-困难

    private String nickName;

    private LocalDateTime createTime;
}

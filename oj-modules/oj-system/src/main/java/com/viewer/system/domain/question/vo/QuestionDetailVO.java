package com.viewer.system.domain.question.vo;


import lombok.Data;

@Data
public class QuestionDetailVO {
    private String title;

    private Integer difficulty;

    private Long timeLimit;

    private Long spaceLimit;

    private String content;

    private String questionCase;

    private String defaultCode;

    private String mainFuc;
}

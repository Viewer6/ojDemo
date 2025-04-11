package com.viewer.common.core.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Difficulty {
    SIMPLE(1, "简单"),
    MIDDLE(2, "中等"),
    HARD(3, "困难");

    private Integer difficulty;
    private String difficultyCN;

}

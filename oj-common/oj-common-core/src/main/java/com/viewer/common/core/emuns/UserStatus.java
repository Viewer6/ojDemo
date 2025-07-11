package com.viewer.common.core.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {

    BLACKLIST(0, "拉黑"),
    NORMAL(1, "正常");

    private Integer value;
    private String desc;
}

package com.viewer.common.core.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserIdentity {

    ORDINARY(1, "普通用户"),
    ADMIN(2, "管理员");

    private Integer value;
    private String desc;
}

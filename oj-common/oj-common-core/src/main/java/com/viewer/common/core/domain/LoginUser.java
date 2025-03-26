package com.viewer.common.core.domain;

import lombok.Data;

@Data
public class LoginUser {
    private Integer identity;  // --1 为普通用户; --2 为管理员用户
}

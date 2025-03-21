package com.viewer.common.core.domain;


import lombok.Data;

import java.time.LocalDateTime;

// 定责类
@Data
public class BaseEntity {
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}

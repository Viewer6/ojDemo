package com.viewer.common.core.domain;

import lombok.Data;

@Data
public class PageQueryDTO {

    private Integer pageSize = 5;

    private Integer pageNum = 1;
}

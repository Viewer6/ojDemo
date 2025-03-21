package com.viewer.system.test.Domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_test")
public class TestDomain {
    @TableId("test_id")
    private Integer testId;
    private String title;
    private String content;
}

package com.viewer.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.viewer.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@TableName("tb_sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID) // 雪花算法自动生成id
    private Long userId;
    private String userAccount;
    private String password;

}

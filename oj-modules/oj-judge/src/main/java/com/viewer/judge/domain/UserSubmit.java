package com.viewer.judge.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.viewer.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户提交记录
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_user_submit")
public class UserSubmit extends BaseEntity {

    @TableId(value = "submit_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long submitId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 竞赛 id，练习提交为 null
     */
    private Long examId;

    /**
     * 编程语言类型 0 java 1 cpp
     */
    private Integer programType;

    /**
     * 用户提交的完整代码
     */
    private String userCode;

    /**
     * 是否通过 0 否 1 是
     */
    private Integer pass;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 执行结果信息
     */
    private String exeMessage;

    /**
     * 每个用例的判题结果（JSON）
     */
    private String caseJudgeRes;
}

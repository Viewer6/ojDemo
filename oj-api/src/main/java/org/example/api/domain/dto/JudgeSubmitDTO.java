package org.example.api.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JudgeSubmitDTO {

    private Long userId;

    private Long examId;

    //编程语言类型（0 java 1 C++）
    private Integer programType;

    private Long questionId;

    //题目难度
    private Integer difficulty;

    //时间限制 ms
    private Long timeLimit;

    //空间限制 kb
    private Long spaceLimit;

    //用户提交的完整 Solution 代码
    private String userCode;

    //题目配置的 main 函数模板（完整的 Main 类），作为独立文件 Main.java 编译
    private String mainFuc;

    //输入数据
    private List<String> inputList;

    //期望输出数据
    private List<String> outputList;
}

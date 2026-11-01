package com.viewer.common.core.constants;

/**
 * 判题模块常量
 */
public class JudgeConstants {

    public static final String ERROR_ANSWER = "未完全通过所有用例";

    public static final String OUT_OF_MEMORY = "超出内存限制，请优化代码";

    public static final String OUT_OF_TIME = "运行时间超出限制，请优化代码";

    public static final Integer ERROR_SCORE = 0;
    public static final Integer DEFAULT_SCORE = 100;

    public static final String EXAM_CODE_DIR = "user-code";

    public static final String CODE_DIR_POOL = "user-code-pool";

    public static final String DOCKER_USER_CODE_DIR = "/usr/share/java";

    public static final String USER_CODE_JAVA_CLASS_NAME = "Solution.java";

    /** 题目配置的 main 函数模板，作为独立文件编译 */
    public static final String MAIN_JAVA_CLASS_NAME = "Main.java";

    public static final String JAVA_ENV_IMAGE = "openjdk:8-jdk-alpine";

    public static final String JAVA_CONTAINER_PREFIX = "/";

    public static final String JAVA_CONTAINER_NAME = "oj-jdk";

    // 用户代码和题目 main 模板分别写成 Solution.java、Main.java，一起编译；
    // 源码由后端以 UTF-8 写入，javac 也要显式按 UTF-8 读取，否则中文注释/字符串会报 unmappable character
    public static final String[] DOCKER_JAVAC_CMD = new String[]{
            "javac", "-encoding", "UTF-8",
            DOCKER_USER_CODE_DIR + "/" + USER_CODE_JAVA_CLASS_NAME,
            DOCKER_USER_CODE_DIR + "/" + MAIN_JAVA_CLASS_NAME
    };

    // main 方法在题目的 main 函数模板（Main 类）里，用户代码只提供 Solution 类，因此入口类是 Main
    public static final String[] DOCKER_JAVA_EXEC_CMD = new String[]{"java", "-Dfile.encoding=UTF-8", "-cp", DOCKER_USER_CODE_DIR, "Main"};
    //  java -Dfile.encoding=UTF-8 -cp /usr/share/java Main

}


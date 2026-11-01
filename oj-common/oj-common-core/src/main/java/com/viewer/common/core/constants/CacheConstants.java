package com.viewer.common.core.constants;

public class CacheConstants {

    public final static String LOGIN_TOKEN_KEY = "logintoken:";

    public final static long EXP = 720;
    public static final long REFRESH_TIME = 360;

    public final static String PHONE_CODE_KEY = "p:c:";

    public final static String CODE_TIME_KEY = "c:t:";

    public final static String EXAM_UNFINISHED_LIST = "e:t:l"; // 未完赛竞赛列表

    public final static String EXAM_HISTORY_LIST = "e:h:l";  // 历史竞赛列表

    public final static String EXAM_DETAIL = "e:d:";    //竞赛详情信息

    public final static String USER_EXAM_LIST = "u:e:l:";   //用户竞赛列表

    public final static String USER_DETAIL = "u:d:";   //用户详情信息

    public final static long USER_EXP = 10;
    public static final String USER_UPLOAD_TIMES_KEY = "u:u:t";

    public static final String QUESTION_LIST = "q:l";

    //题目ES待同步队列：B端增删改题目后把题目id写入这个list，C端检索前按id增量同步
    public static final String QUESTION_ES_PENDING = "q:es:pending";

    //题目ES全量校对标记：定时任务（xxl-job）设置，C端检索前做一次全量校对，兜住sql直接改库等场景
    public static final String QUESTION_ES_FULL_SYNC = "q:es:fullsync";

    public static final String QUESTION_HOST_LIST = "q:h:l";

    public static final String EXAM_QUESTION_LIST = "e:q:l:";

    public static final String USER_MESSAGE_LIST = "u:m:l:";

    public static final String MESSAGE_DETAIL = "m:d:";

    public static final String EXAM_RANK_LIST = "e:r:l:";

    public static final long DEFAULT_START = 0;

    public static final long DEFAULT_END = -1;

}

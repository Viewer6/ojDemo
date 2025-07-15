package com.viewer.system.manager;

import com.viewer.common.core.constants.CacheConstants;
import com.viewer.common.redis.service.RedisService;
import com.viewer.system.domain.exam.Exam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExamCacheManager {

    @Autowired
    private RedisService redisService;

    /**
     * 往redis里面存数据
     * @param exam
     */
    public void addCache(Exam exam) {
        redisService.leftPushForList(getExamListKey(), exam.getExamId());
        redisService.setCacheObject(getDetailKey(exam.getExamId()), exam);
    }

    /**
     * 从redis里面删数据
     * @param examId
     */
    public void deleteCache(Long examId) {
        redisService.removeForList(getExamListKey(), examId);
        redisService.deleteObject(getDetailKey(examId));
        redisService.deleteObject(getExamQuestionListKey(examId));
    }

    private String getExamListKey() {
        return CacheConstants.EXAM_UNFINISHED_LIST;
    }

    private String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL + examId;
    }

    private String getExamQuestionListKey(Long examId) {
        return CacheConstants.EXAM_QUESTION_LIST + examId;
    }
}
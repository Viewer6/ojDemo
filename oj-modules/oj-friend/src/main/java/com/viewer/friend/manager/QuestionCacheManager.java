package com.viewer.friend.manager;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.viewer.common.core.constants.CacheConstants;
import com.viewer.common.core.enums.ResultCode;
import com.viewer.common.core.exception.ServiceException;
import com.viewer.common.redis.service.RedisService;
import com.viewer.friend.domain.question.Question;
import com.viewer.friend.mapper.question.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionCacheManager {

    @Autowired
    private RedisService redisService;

    @Autowired
    private QuestionMapper questionMapper;

    public Long getListSize() {
        return redisService.getListSize(CacheConstants.QUESTION_LIST);
    }

    public Long getHostListSize() {
        return redisService.getListSize(CacheConstants.QUESTION_HOST_LIST);
    }

    public synchronized void refreshCache() {
        // 先清空旧的题目顺序缓存，避免数据库增删题目后重建时又追加出重复的题目id
        redisService.deleteObject(CacheConstants.QUESTION_LIST);
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .select(Question::getQuestionId)
                //和题目列表保持一致：创建时间相同时按题目id倒序，保证上一题/下一题顺序稳定
                .orderByDesc(Question::getCreateTime, Question::getQuestionId));
        if (CollectionUtil.isEmpty(questionList)) {
            return;
        }
        List<Long> questionIdList = questionList.stream().map(Question::getQuestionId).toList();
        redisService.rightPushAll(CacheConstants.QUESTION_LIST, questionIdList);
    }

    public Long preQuestion(Long questionId) {
//        List<Long> list = redisService.getCacheListByRange(CacheConstants.QUESTION_LIST, 0, -1, Long.class);
        Long index = redisService.indexOfForList(CacheConstants.QUESTION_LIST, questionId);
        if (index == 0) {
            throw new ServiceException(ResultCode.FAILED_FIRST_QUESTION);
        }
        return redisService.indexForList(CacheConstants.QUESTION_LIST, index - 1, Long.class);
    }

    public Object nextQuestion(Long questionId) {
        Long index = redisService.indexOfForList(CacheConstants.QUESTION_LIST, questionId);
        long lastIndex = getListSize() - 1;
        if (index == lastIndex) {
            throw new ServiceException(ResultCode.FAILED_LAST_QUESTION);
        }
        return redisService.indexForList(CacheConstants.QUESTION_LIST, index + 1, Long.class);
    }

    public List<Long> getHostList() {
        return redisService.getCacheListByRange(CacheConstants.QUESTION_HOST_LIST,
                CacheConstants.DEFAULT_START, CacheConstants.DEFAULT_END, Long.class);
    }

    public void refreshHotQuestionList(List<Long> hotQuestionIdList) {
        if (CollectionUtil.isEmpty(hotQuestionIdList)) {
            return;
        }
        redisService.rightPushAll(CacheConstants.QUESTION_HOST_LIST, hotQuestionIdList);
    }
}

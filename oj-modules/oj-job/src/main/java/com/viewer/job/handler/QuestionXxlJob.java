package com.viewer.job.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.viewer.common.core.constants.CacheConstants;
import com.viewer.common.core.constants.Constants;
import com.viewer.common.redis.service.RedisService;
import com.viewer.job.mapper.user.UserSubmitMapper;
import com.github.pagehelper.PageHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class QuestionXxlJob {

    @Autowired
    private UserSubmitMapper userSubmitMapper;

    @Autowired
    private RedisService redisService;

    @XxlJob("hostQuestionListHandler")
    public void hostQuestionListHandler() {
        log.info("----- 题目热门列表统计开始 ------");
        PageHelper.startPage(Constants.HOST_QUESTION_LIST_START, Constants.HOST_QUESTION_LIST_END);
        List<Long> questionIdList = userSubmitMapper.selectHostQuestionList();
        if (CollectionUtil.isEmpty(questionIdList)) {
            return;
        }
        redisService.deleteObject(CacheConstants.QUESTION_HOST_LIST);
        redisService.rightPushAll(CacheConstants.QUESTION_HOST_LIST, questionIdList);
        log.info("----- 题目热门列表统计结束 ------");
    }

    /**
     * 题目ES索引全量校对标记。
     * <p>
     * 这里只设置标记，真正的校对由C端（oj-friend）在查询题目列表时执行，
     * 这样job模块不用依赖ES，也能兜住“直接执行SQL改题目内容、数量没变”这类场景。
     * 在xxl-job调度中心配置成每天凌晨0点执行即可。
     */
    @XxlJob("questionIndexSyncHandler")
    public void questionIndexSyncHandler() {
        log.info("----- 设置题目ES索引全量校对标记开始 ------");
        redisService.setCacheObject(CacheConstants.QUESTION_ES_FULL_SYNC, System.currentTimeMillis());
        log.info("----- 设置题目ES索引全量校对标记结束 ------");
    }
}

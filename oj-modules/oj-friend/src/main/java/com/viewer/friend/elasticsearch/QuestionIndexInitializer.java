package com.viewer.friend.elasticsearch;

import com.viewer.friend.manager.QuestionIndexManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时和MySQL校对一次题目ES索引。
 * <p>
 * 直接执行 SQL 脚本增删改 tb_question 不会经过后台管理接口，
 * 启动时做一次全量校对（只写有变化的文档），重启后就能检索到最新题目。
 */
@Component
@Slf4j
public class QuestionIndexInitializer implements ApplicationRunner {

    @Autowired
    private QuestionIndexManager questionIndexManager;

    @Override
    public void run(ApplicationArguments args) {
        try {
            questionIndexManager.reconcile();
        } catch (Exception e) {
            log.warn("启动时校对题目ES索引失败，题目列表查询时会再次尝试", e);
        }
    }
}

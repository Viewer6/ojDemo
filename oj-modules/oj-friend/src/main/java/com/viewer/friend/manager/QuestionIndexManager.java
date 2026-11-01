package com.viewer.friend.manager;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.viewer.common.core.constants.CacheConstants;
import com.viewer.common.redis.service.RedisService;
import com.viewer.friend.domain.question.Question;
import com.viewer.friend.domain.question.es.QuestionES;
import com.viewer.friend.elasticsearch.QuestionRepository;
import com.viewer.friend.mapper.question.QuestionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 题目ES索引同步。
 * <p>
 * MySQL 是题目数据的唯一来源，ES 只负责检索。同步分三档：
 * <ul>
 *     <li>增量同步：B端增删改题目后，把题目id写入Redis队列，C端检索前只同步这几道题；</li>
 *     <li>数量校对：直接执行SQL脚本增删题目时，ES和MySQL的题目数量对不上，触发一次全量校对；</li>
 *     <li>定时全量校对：xxl-job每天凌晨设置全量校对标记，兜住“直接SQL改题目内容、数量没变”这类场景。</li>
 * </ul>
 * 全量校对同样只写有变化的文档，不会清空重建索引。
 */
@Component
@Slf4j
public class QuestionIndexManager {

    /**
     * 一次最多处理的增量同步id数量，避免积压过多时单次请求耗时太长
     */
    private static final int MAX_PENDING_SIZE = 500;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    /**
     * 题目列表查询前调用，保证ES索引和MySQL一致。
     */
    public void syncIfNeeded() {
        //定时任务要求全量校对
        if (takeFullSyncFlag()) {
            reconcile();
        }
        //B端增删改题目产生的增量同步
        List<Long> pendingIds = drainPendingIds();
        if (!pendingIds.isEmpty()) {
            syncQuestions(pendingIds);
        }
        //直接执行SQL脚本增删题目：数量对不上时全量校对
        if (needFullSync()) {
            synchronized (this) {
                if (needFullSync()) {
                    reconcile();
                }
            }
        }
    }

    /**
     * 增量同步指定的几道题：数据库里有就更新ES文档，没有了就从ES删除。
     */
    public synchronized void syncQuestions(List<Long> questionIds) {
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .in(Question::getQuestionId, questionIds));
        Map<Long, Question> questionMap = new HashMap<>();
        for (Question question : questionList) {
            questionMap.put(question.getQuestionId(), question);
        }
        boolean changed = false;
        for (Long questionId : questionIds) {
            Question question = questionMap.get(questionId);
            try {
                if (question == null) {
                    //题目已经被删除
                    questionRepository.deleteById(questionId);
                } else {
                    questionRepository.save(BeanUtil.copyProperties(question, QuestionES.class));
                }
                changed = true;
            } catch (Exception e) {
                log.warn("题目[{}]同步到ES失败", questionId, e);
            }
        }
        if (changed) {
            refreshIndex();
        }
    }

    /**
     * 全量校对：逐条比较MySQL和ES中的题目，只写有变化的文档，并删除ES中数据库已经删掉的文档。
     */
    public synchronized void reconcile() {
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>());
        Map<Long, QuestionES> esQuestionMap = new HashMap<>();
        try {
            for (QuestionES questionES : questionRepository.findAll()) {
                esQuestionMap.put(questionES.getQuestionId(), questionES);
            }
        } catch (Exception e) {
            //索引不存在时读取会失败，按空索引处理，后面会重新写入
            log.warn("读取ES题目索引失败，按空索引处理", e);
        }
        int updateCount = 0;
        int deleteCount = 0;
        for (Question question : questionList) {
            QuestionES questionES = esQuestionMap.remove(question.getQuestionId());
            if (questionES == null || !isSameQuestion(questionES, question)) {
                try {
                    questionRepository.save(BeanUtil.copyProperties(question, QuestionES.class));
                    updateCount++;
                } catch (Exception e) {
                    log.warn("题目[{}]同步到ES失败", question.getQuestionId(), e);
                }
            }
        }
        //剩下的都是ES里有、数据库里已经没有的文档
        for (QuestionES questionES : esQuestionMap.values()) {
            try {
                questionRepository.deleteById(questionES.getQuestionId());
                deleteCount++;
            } catch (Exception e) {
                log.warn("从ES删除题目[{}]失败", questionES.getQuestionId(), e);
            }
        }
        if (updateCount > 0 || deleteCount > 0) {
            refreshIndex();
        }
        log.info("题目ES索引校对完成，更新{}道，删除{}道", updateCount, deleteCount);
    }

    /**
     * 把单道题目的最新数据写回ES，失败不影响题目查询。
     */
    public void refreshQuestion(Question question) {
        try {
            questionRepository.save(BeanUtil.copyProperties(question, QuestionES.class));
        } catch (Exception e) {
            log.warn("题目[{}]同步到ES失败，不影响题目查询", question.getQuestionId(), e);
        }
    }

    /**
     * 从Redis队列中取出待同步的题目id，取出即删除，失败时下次继续。
     */
    private List<Long> drainPendingIds() {
        List<Long> pendingIds = new ArrayList<>();
        try {
            while (pendingIds.size() < MAX_PENDING_SIZE) {
                Long questionId = redisService.leftPopForList(CacheConstants.QUESTION_ES_PENDING, Long.class);
                if (questionId == null) {
                    break;
                }
                pendingIds.add(questionId);
            }
        } catch (Exception e) {
            log.warn("读取题目ES待同步队列失败", e);
        }
        return pendingIds;
    }

    /**
     * 取全量校对标记（取到后就删掉，只校对一次）。
     */
    private boolean takeFullSyncFlag() {
        try {
            if (Boolean.TRUE.equals(redisService.hasKey(CacheConstants.QUESTION_ES_FULL_SYNC))) {
                redisService.deleteObject(CacheConstants.QUESTION_ES_FULL_SYNC);
                return true;
            }
        } catch (Exception e) {
            log.warn("检查题目ES索引全量校对标记失败", e);
        }
        return false;
    }

    private boolean needFullSync() {
        Long questionCount = questionMapper.selectCount(null);
        try {
            return questionCount == null || questionCount != questionRepository.count();
        } catch (Exception e) {
            //索引不存在时count会抛异常，按需要全量校对处理
            log.warn("查询ES题目数量失败，准备全量校对题目索引", e);
            return true;
        }
    }

    private void refreshIndex() {
        try {
            //ES默认1秒才刷新一次，这里手动刷新，保证同步后马上就能检索到最新题目
            elasticsearchOperations.indexOps(QuestionES.class).refresh();
        } catch (Exception e) {
            log.warn("刷新ES题目索引失败", e);
        }
    }

    /**
     * 比较数据库题目和ES文档的内容是否一致，用于全量校对时跳过没变化的文档。
     */
    private boolean isSameQuestion(QuestionES questionES, Question question) {
        return Objects.equals(questionES.getTitle(), question.getTitle())
                && Objects.equals(questionES.getDifficulty(), question.getDifficulty())
                && Objects.equals(questionES.getTimeLimit(), question.getTimeLimit())
                && Objects.equals(questionES.getSpaceLimit(), question.getSpaceLimit())
                && Objects.equals(questionES.getContent(), question.getContent())
                && Objects.equals(questionES.getQuestionCase(), question.getQuestionCase())
                && Objects.equals(questionES.getDefaultCode(), question.getDefaultCode())
                && Objects.equals(questionES.getMainFuc(), question.getMainFuc())
                && isSameCreateTime(questionES.getCreateTime(), question.getCreateTime());
    }

    private boolean isSameCreateTime(LocalDateTime esCreateTime, LocalDateTime dbCreateTime) {
        if (esCreateTime == null || dbCreateTime == null) {
            return esCreateTime == dbCreateTime;
        }
        //ES里的时间只精确到秒，比较前一起截断
        return esCreateTime.truncatedTo(ChronoUnit.SECONDS)
                .equals(dbCreateTime.truncatedTo(ChronoUnit.SECONDS));
    }
}

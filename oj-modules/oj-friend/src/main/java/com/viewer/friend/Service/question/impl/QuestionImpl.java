package com.viewer.friend.Service.question.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.viewer.common.core.constants.Constants;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.friend.Service.question.IQuestionService;
import com.viewer.friend.domain.question.Question;
import com.viewer.friend.domain.question.dto.QuestionQueryDTO;
import com.viewer.friend.domain.question.es.QuestionES;
import com.viewer.friend.domain.question.vo.QuestionDetailVO;
import com.viewer.friend.domain.question.vo.QuestionVO;
import com.viewer.friend.elasticsearch.QuestionRepository;
import com.viewer.friend.manager.QuestionCacheManager;
import com.viewer.friend.manager.QuestionIndexManager;
import com.viewer.friend.mapper.question.QuestionMapper;
import com.viewer.friend.mapper.user.UserSubmitMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class QuestionImpl implements IQuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionIndexManager questionIndexManager;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserSubmitMapper userSubmitMapper;

    @Autowired
    private QuestionCacheManager questionCacheManager;

    @Override
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO) {
        //从ES检索之前，先确认ES索引和MySQL一致：
        //B端增删改题目、或直接执行SQL脚本改库后，这里会先把最新题目同步到ES，避免一直检索到旧题目
        questionIndexManager.syncIfNeeded();

        //按创建时间倒序；很多题目是同一秒插入的（createTime相同），必须再加题目id作为兜底排序，
        //否则ES对相同排序值的先后顺序不固定，每次刷新列表顺序都会变，翻页还会重复/漏题
        Sort sort = Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("questionId"));
        Pageable pageable = PageRequest.of(questionQueryDTO.getPageNum() - 1, questionQueryDTO.getPageSize(), sort);
        Integer difficulty = questionQueryDTO.getDifficulty();
        String keyword = questionQueryDTO.getKeyword();
        Page<QuestionES> questionESPage;
        if (difficulty == null && StrUtil.isEmpty(keyword)) {
            questionESPage = questionRepository.findAll(pageable);
        } else if (StrUtil.isEmpty(keyword)) {
            questionESPage = questionRepository.findQuestionByDifficulty(difficulty, pageable);
        } else if (difficulty == null) {
            questionESPage = questionRepository.findByTitleOrContent(keyword, keyword, pageable);
        } else {
            questionESPage = questionRepository.findByTitleOrContentAndDifficulty(keyword, keyword, difficulty, pageable);
        }
        long total = questionESPage.getTotalElements();
        if (total <= 0) {
            return TableDataInfo.empty();
        }
        List<QuestionVO> questionVOList = BeanUtil.copyToList(questionESPage.getContent(), QuestionVO.class);
        return TableDataInfo.success(questionVOList, total);
    }

    @Override
    public List<QuestionVO> hotList() {
        Long total = questionCacheManager.getHostListSize();
        List<Long> hotQuestionIdList;
        if (total == null || total <= 0) {
            PageHelper.startPage(Constants.HOST_QUESTION_LIST_START, Constants.HOST_QUESTION_LIST_END);
            hotQuestionIdList = userSubmitMapper.selectHostQuestionList();
            questionCacheManager.refreshHotQuestionList(hotQuestionIdList);
        } else {
            hotQuestionIdList = questionCacheManager.getHostList();
        }
        return assembleQuestionVOList(hotQuestionIdList);
    }

    @Override
    public QuestionDetailVO detail(Long questionId) {
        //详情以MySQL为准，避免ES索引还没同步时把旧题面返回给前端
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            return null;
        }
        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        BeanUtil.copyProperties(question, questionDetailVO);
        //顺手把这道题的最新数据写回ES，判题时读ES也能拿到最新题目
        questionIndexManager.refreshQuestion(question);
        return questionDetailVO;
    }

    @Override
    public String preQuestion(Long questionId) {
        refreshQuestionListCacheIfNeeded();
        return questionCacheManager.preQuestion(questionId).toString();
    }

    @Override
    public String nextQuestion(Long questionId) {
        refreshQuestionListCacheIfNeeded();
        return questionCacheManager.nextQuestion(questionId).toString();
    }

    /**
     * 题目顺序缓存（Redis list）原来只在缓存为空时才从数据库重建，
     * 数据库增删题目后缓存不会失效，新题目在上一题/下一题里就找不到。
     * 这里用数据库题目总数和缓存长度做对比，不一致时重建缓存。
     */
    private void refreshQuestionListCacheIfNeeded() {
        Long listSize = questionCacheManager.getListSize();
        Long questionCount = questionMapper.selectCount(null);
        if (listSize == null || listSize <= 0 || !listSize.equals(questionCount)) {
            questionCacheManager.refreshCache();
        }
    }

    private List<QuestionVO> assembleQuestionVOList(List<Long> hotQuestionIdList) {
        if (CollectionUtil.isEmpty(hotQuestionIdList)) {
            return new ArrayList<>();
        }
        List<QuestionVO> resultList = new ArrayList<>();
        for (Long questionId : hotQuestionIdList) {
            QuestionVO questionVO = new QuestionVO();
            QuestionDetailVO detail = detail(questionId);
            if (detail == null) {
                //题目已被删除，跳过，避免热榜因为脏数据报错
                continue;
            }
            questionVO.setTitle(detail.getTitle());
            resultList.add(questionVO);
        }
        return resultList;
    }
}

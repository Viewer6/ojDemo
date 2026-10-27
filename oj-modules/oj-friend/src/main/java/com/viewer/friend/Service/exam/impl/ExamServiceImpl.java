package com.viewer.friend.Service.exam.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.viewer.common.core.constants.Constants;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.common.core.utils.ThreadLocalUtil;
import com.viewer.friend.domain.exam.dto.ExamQueryDTO;
import com.viewer.friend.domain.exam.dto.ExamRankDTO;
import com.viewer.friend.domain.exam.vo.ExamListVO;
import com.viewer.friend.Service.exam.IExamService;
import com.viewer.friend.domain.exam.vo.ExamRankVO;
import com.viewer.friend.domain.user.vo.UserVO;
import com.viewer.friend.manager.ExamCacheManager;
import com.viewer.friend.manager.UserCacheManager;
import com.viewer.friend.mapper.exam.ExamMapper;
import com.viewer.friend.mapper.user.UserExamMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamServiceImpl implements IExamService {
    @Resource(name = "examMapper")
    private ExamMapper examMapper;

    @Autowired
    private ExamCacheManager examCacheManager;

    @Autowired
    private UserCacheManager userCacheManager;

    @Autowired
    private UserExamMapper userExamMapper;

    @Override
    public List<ExamListVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public TableDataInfo redisGetList(ExamQueryDTO examQueryDTO) {
        //从redis当中获取  竞赛列表的数据
        Long total = examCacheManager.getListSize(examQueryDTO.getType());
        List<ExamListVO> examVOList;
        if (total == null || total <= 0) { // redis中数据为空情况
            examVOList = list(examQueryDTO); // 直接从数据库获取数据
            examCacheManager.refreshCache(examQueryDTO.getType());
            total = new PageInfo<>(examVOList).getTotal();
        } else {
            examVOList = examCacheManager.getExamVOList(examQueryDTO);
            total = examCacheManager.getListSize(examQueryDTO.getType());
        }
        if (CollectionUtil.isEmpty(examVOList)) {
            return TableDataInfo.empty();
        }
        assembleExamVOList(examVOList);
        return TableDataInfo.success(examVOList, total);
    }

    @Override
    public TableDataInfo rankList(ExamRankDTO examRankDTO) {
        Long total = examCacheManager.getRankListSize(examRankDTO.getExamId());
        List<ExamRankVO> examRankVOList;
        if (total == null || total <= 0) {
            PageHelper.startPage(examRankDTO.getPageNum(), examRankDTO.getPageSize());
            examRankVOList = userExamMapper.selectExamRankList(examRankDTO.getExamId());
            examCacheManager.refreshExamRankCache(examRankDTO.getExamId());
            total = new PageInfo<>(examRankVOList).getTotal();
        } else {
            examRankVOList = examCacheManager.getExamRankList(examRankDTO);
        }
        if (CollectionUtil.isEmpty(examRankVOList)) {
            return TableDataInfo.empty();
        }
        assembleExamRankVOList(examRankVOList);
        return TableDataInfo.success(examRankVOList, total);
    }

    @Override
    public String getFirstQuestion(Long examId) {
        return "";
    }

    @Override
    public String preQuestion(Long examId, Long questionId) {
        return "";
    }

    @Override
    public String nextQuestion(Long examId, Long questionId) {
        return "";
    }


    /**
     * 游客登录/用户登录
     */
    private void assembleExamVOList(List<ExamListVO> examVOList) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        List<Long> userExamIdList = examCacheManager.getAllUserExamList(userId);
        if (CollectionUtil.isEmpty(userExamIdList)) {
            return;
        }
        for (ExamListVO examVO : examVOList) {
            if (userExamIdList.contains(examVO.getExamId())) {
                examVO.setEnter(true);
            }
        }
    }

    private void assembleExamRankVOList(List<ExamRankVO> examRankVOList) {
        if (CollectionUtil.isEmpty(examRankVOList)) {
            return;
        }
        for (ExamRankVO examRankVO : examRankVOList) {
            Long userId = examRankVO.getUserId();
            UserVO user = userCacheManager.getUserById(userId);
            examRankVO.setNickName(user.getNickName());
        }
    }
}

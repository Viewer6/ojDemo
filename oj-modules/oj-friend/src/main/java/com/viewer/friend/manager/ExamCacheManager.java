package com.viewer.friend.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.viewer.common.core.constants.CacheConstants;
import com.viewer.common.core.constants.Constants;
import com.viewer.common.core.enums.ExamListType;
import com.viewer.common.redis.service.RedisService;
import com.viewer.friend.domain.exam.Exam;
import com.viewer.friend.domain.exam.dto.ExamQueryDTO;
import com.viewer.friend.domain.exam.dto.ExamRankDTO;
import com.viewer.friend.domain.exam.vo.ExamListVO;
import com.viewer.friend.domain.exam.vo.ExamRankVO;
import com.viewer.friend.domain.user.UserExam;
import com.viewer.friend.mapper.exam.ExamMapper;
import com.viewer.friend.mapper.user.UserExamMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExamCacheManager {

    @Resource(name = "examMapper")
    private ExamMapper examMapper;

    @Resource(name = "userExamMapper")
    private UserExamMapper userExamMapper;

    @Autowired
    private RedisService redisService;

    // 获取列表大小
    public Long getListSize(Integer examListType) {
        String examListKey = getExamListKey(examListType);
        return redisService.getListSize(examListKey);
    }
    public Long getListSize(Integer examListType, Long userId) {
        String examListKey = getExamListKey(examListType, userId);
        return redisService.getListSize(examListKey);
    }

    public Long getRankListSize(Long examId) {
        return redisService.getListSize(getExamRankListKey(examId));
    }

    private String getExamRankListKey(Long examId) {
        return CacheConstants.EXAM_RANK_LIST + examId;
    }

    //刷新缓存逻辑
    public void refreshCache(Integer examListType, Long userId) {
        List<Exam> examList = new ArrayList<>();
        if (ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(examListType)) {
            //查询未完赛的竞赛列表
            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                    .gt(Exam::getEndTime, LocalDateTime.now())
                    .eq(Exam::getStatus, Constants.TRUE)
                    .orderByDesc(Exam::getCreateTime));
        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)) {
            //查询历史竞赛
            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                    .le(Exam::getEndTime, LocalDateTime.now())
                    .eq(Exam::getStatus, Constants.TRUE)
                    .orderByDesc(Exam::getCreateTime));
        } else if (ExamListType.USER_EXAM_LIST.getValue().equals(examListType)) {
            List<ExamListVO> examVOList = userExamMapper.selectUserExamList(userId);
            examList = BeanUtil.copyToList(examVOList, Exam.class);
        }
        if (CollectionUtil.isEmpty(examList)) {
            return;
        }

        Map<String, Exam> examMap = new HashMap<>();
        List<Long> examIdList = new ArrayList<>();
        for (Exam exam : examList) {
            examMap.put(getDetailKey(exam.getExamId()), exam);
            examIdList.add(exam.getExamId());
        }
        redisService.multiSet(examMap);  //刷新详情缓存
        redisService.deleteObject(getExamListKey(examListType, userId));
        redisService.rightPushAll(getExamListKey(examListType, userId), examIdList);      //刷新列表缓存
    }

    // 获取竞赛列表
    public List<ExamListVO> getExamVOList(ExamQueryDTO examQueryDTO) {
        // 计算分页开始下标
        int start = (examQueryDTO.getPageNum() - 1) * examQueryDTO.getPageSize();
        int end = start + examQueryDTO.getPageSize() - 1; //下标需要 -1

        String examListKey = getExamListKey(examQueryDTO.getType());
        // 获取题目id列表
        List<Long> examIdList = redisService.getCacheListByRange(examListKey, start, end, Long.class);
        // 根据题目id列表获取题目详细信息列表
        List<ExamListVO> examVOList = assembleExamVOList(examIdList);

        if (CollectionUtil.isEmpty(examVOList)) {
            //说明redis中数据可能有问题 从数据库中查数据并且重新刷新缓存
            examVOList = getExamListByDB(examQueryDTO); //从数据库中获取数据
            refreshCache(examQueryDTO.getType()); //更新数据
        }
        return examVOList;
    }

    public List<ExamListVO> getExamVOList(ExamQueryDTO examQueryDTO, Long userId) {
        int start = (examQueryDTO.getPageNum() - 1) * examQueryDTO.getPageSize();
        int end = start + examQueryDTO.getPageSize() - 1; //下标需要 -1
        String examListKey = getExamListKey(examQueryDTO.getType(), userId);
        List<Long> examIdList = redisService.getCacheListByRange(examListKey, start, end, Long.class);
        List<ExamListVO> examVOList = assembleExamVOList(examIdList);
        if (CollectionUtil.isEmpty(examVOList)) {
            //说明redis中数据可能有问题 从数据库中查数据并且重新刷新缓存
            examVOList = getExamListByDB(examQueryDTO, userId); //从数据库中获取数据
            refreshCache(examQueryDTO.getType(), userId);
        }
        return examVOList;
    }

    //刷新缓存逻辑
    public void refreshCache(Integer examListType) {
        List<Exam> examList = new ArrayList<>();
        if (ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(examListType)) {
            // 查询未完赛的竞赛列表
            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                    .gt(Exam::getEndTime, LocalDateTime.now())
                    .eq(Exam::getStatus, Constants.TRUE)
                    .orderByDesc(Exam::getCreateTime));
        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)) {
            // 查询历史竞赛
            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                    .le(Exam::getEndTime, LocalDateTime.now())
                    .eq(Exam::getStatus, Constants.TRUE)
                    .orderByDesc(Exam::getCreateTime));
        }
        // 如果竞赛为null直接返回
        if (CollectionUtil.isEmpty(examList)) {
            return;
        }

        // 更新redis中数据
        Map<String, Exam> examMap = new HashMap<>(); //竞赛详情
        List<Long> examIdList = new ArrayList<>(); //竞赛id
        for (Exam exam : examList) {
            examMap.put(getDetailKey(exam.getExamId()), exam);
            examIdList.add(exam.getExamId());
        }
        redisService.multiSet(examMap);  //刷新详情缓存
        redisService.deleteObject(getExamListKey(examListType)); //删除老数据, 防止数据重复
        redisService.rightPushAll(getExamListKey(examListType), examIdList); //刷新列表缓存
    }

    public void refreshExamRankCache(Long examId) {
        List<ExamRankVO> examRankVOList = userExamMapper.selectExamRankList(examId);
        if (CollectionUtil.isEmpty(examRankVOList)) {
            return;
        }
        redisService.rightPushAll(getExamRankListKey(examId), examRankVOList);
    }

    public List<ExamRankVO> getExamRankList(ExamRankDTO examRankDTO) {
        int start = (examRankDTO.getPageNum() - 1) * examRankDTO.getPageSize();
        int end = start + examRankDTO.getPageSize() - 1; //下标需要 -1
        return redisService.getCacheListByRange(getExamRankListKey(examRankDTO.getExamId()), start, end, ExamRankVO.class);
    }

    public List<Long> getAllUserExamList(Long userId) {
        String examListKey = CacheConstants.USER_EXAM_LIST + userId;
        List<Long> userExamIdList = redisService.getCacheListByRange(examListKey, 0, -1, Long.class);
        if (CollectionUtil.isNotEmpty(userExamIdList)) {
            return userExamIdList;
        }
        List<UserExam> userExamList =
                userExamMapper.selectList(new LambdaQueryWrapper<UserExam>().eq(UserExam::getUserId, userId));
        if (CollectionUtil.isEmpty(userExamList)) {
            return null;
        }
        refreshCache(ExamListType.USER_EXAM_LIST.getValue(), userId);
        return userExamList.stream().map(UserExam::getExamId).collect(Collectors.toList());
    }

    // 从数据库里面获取数据
    private List<ExamListVO> getExamListByDB(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    private List<ExamListVO> getExamListByDB(ExamQueryDTO examQueryDTO, Long userId) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        if (ExamListType.USER_EXAM_LIST.getValue().equals(examQueryDTO.getType())) {
            //查询我的竞赛列表
            return userExamMapper.selectUserExamList(userId);
        } else {
            //查询C端的竞赛列表
            return examMapper.selectExamList(examQueryDTO);
        }
    }

    // 根据题目id列表获取题目详细信息列表
    private List<ExamListVO> assembleExamVOList(List<Long> examIdList) {
        if (CollectionUtil.isEmpty(examIdList)) {
            //说明redis当中没数据 从数据库中查数据并且重新刷新缓存
            return null;
        }
        //拼接redis当中key的方法 并且将拼接好的key存储到一个list中
        List<String> detailKeyList = new ArrayList<>();
        for (Long examId : examIdList) {
            detailKeyList.add(getDetailKey(examId));
        }
        // 调用multiGet方法, 批量获取竞赛
        List<ExamListVO> examVOList = redisService.multiGet(detailKeyList, ExamListVO.class);
        CollUtil.removeNull(examVOList);
        if (CollectionUtil.isEmpty(examVOList) || examVOList.size() != examIdList.size()) {
            //说明redis中数据有问题 从数据库中查数据并且重新刷新缓存
            return null;
        }
        return examVOList;
    }

    /**
     * 参加竞赛加入缓存
     * @param userId
     * @param examId
     */
    public void addUserExamCache(Long userId, Long examId) {
        String userExamListKey = getUserExamListKey(userId);
        redisService.leftPushForList(userExamListKey, examId);
    }

    /**
     * 根据要获取的
     * @param examListType
     * @return
     */
    private String getExamListKey(Integer examListType) {
        if (ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(examListType)) {
            return CacheConstants.EXAM_UNFINISHED_LIST;
        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)) {
            return CacheConstants.EXAM_HISTORY_LIST;
        }
        return "";
    }

    private String getExamListKey(Integer examListType, Long userId) {
        if (ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(examListType)) {
            return CacheConstants.EXAM_UNFINISHED_LIST;
        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)) {
            return CacheConstants.EXAM_HISTORY_LIST;
        } else {
            return CacheConstants.USER_EXAM_LIST + userId;
        }
    }

    /**
     * 获取题目细节关键字
     * @param examId
     * @return
     */
    private String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL + examId;
    }




    private String getUserExamListKey(Long userId) {
        return CacheConstants.USER_EXAM_LIST + userId;
    }
}
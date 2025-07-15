package com.viewer.friend.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.viewer.common.core.constants.CacheConstants;
import com.viewer.common.core.constants.Constants;
import com.viewer.common.core.emuns.ExamListType;
import com.viewer.common.redis.service.RedisService;
import com.viewer.friend.domain.exam.Exam;
import com.viewer.friend.domain.exam.dto.ExamQueryDTO;
import com.viewer.friend.domain.exam.vo.ExamListVO;
import com.viewer.friend.mapper.exam.ExamMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExamCacheManager {

    @Resource(name = "examMapper")
    private ExamMapper examMapper;

    @Autowired
    private RedisService redisService;

    // 获取列表大小
    public Long getListSize(Integer examListType) {
        String examListKey = getExamListKey(examListType);
        return redisService.getListSize(examListKey);
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

    // 从数据库里面获取数据
    private List<ExamListVO> getExamListByDB(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
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

    /**
     * 获取题目细节关键字
     * @param examId
     * @return
     */
    private String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL + examId;
    }
}
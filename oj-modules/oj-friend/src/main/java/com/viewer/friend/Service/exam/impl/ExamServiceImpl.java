package com.viewer.friend.Service.exam.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.friend.domain.exam.dto.ExamQueryDTO;
import com.viewer.friend.domain.exam.vo.ExamListVO;
import com.viewer.friend.Service.exam.IExamService;
import com.viewer.friend.manager.ExamCacheManager;
import com.viewer.friend.mapper.exam.ExamMapper;
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
        return TableDataInfo.success(examVOList, total);
    }
}

package com.viewer.friend.Service.exam;

import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.friend.domain.exam.dto.ExamQueryDTO;
import com.viewer.friend.domain.exam.vo.ExamListVO;

import java.util.List;

public interface IExamService {
    List<ExamListVO> list(ExamQueryDTO examQueryDTO);

    TableDataInfo redisGetList(ExamQueryDTO examQueryDTO);
}

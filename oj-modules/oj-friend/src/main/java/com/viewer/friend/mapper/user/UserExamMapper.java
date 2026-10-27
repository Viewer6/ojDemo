package com.viewer.friend.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viewer.friend.domain.exam.vo.ExamRankVO;
import com.viewer.friend.domain.exam.vo.ExamListVO;
import com.viewer.friend.domain.user.UserExam;

import java.util.List;


public interface UserExamMapper extends BaseMapper<UserExam> {

    List<ExamListVO> selectUserExamList(Long userId);

    List<ExamRankVO> selectExamRankList(Long examId);

}
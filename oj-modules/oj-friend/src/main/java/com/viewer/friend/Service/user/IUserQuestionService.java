package com.viewer.friend.Service.user;


import com.viewer.common.core.domain.Result;
import com.viewer.friend.domain.user.dot.UserSubmitDTO;
import org.example.api.domain.vo.UserQuestionResultVO;

public interface IUserQuestionService {
    Result<UserQuestionResultVO> submit(UserSubmitDTO submitDTO);

    boolean rabbitSubmit(UserSubmitDTO submitDTO);

    UserQuestionResultVO exeResult(Long examId, Long questionId, String currentTime);
}

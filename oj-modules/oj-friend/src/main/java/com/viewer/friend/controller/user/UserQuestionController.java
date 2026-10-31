package com.viewer.friend.controller.user;

import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.Result;
import com.viewer.friend.Service.user.IUserQuestionService;
import com.viewer.friend.domain.user.dot.UserSubmitDTO;
import lombok.extern.slf4j.Slf4j;
import org.example.api.domain.vo.UserQuestionResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user/question")
public class UserQuestionController extends BaseController {

    @Autowired
    private IUserQuestionService userQuestionService;

    //用户代码提交   请求方法  地址  参数  响应数据结构
    @PostMapping("/submit")
    public Result<UserQuestionResultVO> submit(@RequestBody UserSubmitDTO submitDTO) {
        return userQuestionService.submit(submitDTO);
    }

    @PostMapping("/rabbit/submit")
    public Result<Void> rabbitSubmit(@RequestBody UserSubmitDTO submitDTO) {
        log.info(submitDTO.getUserCode());
        return getResult(userQuestionService.rabbitSubmit(submitDTO));
    }

    @GetMapping("/exe/result")
    public  Result<UserQuestionResultVO> exeResult(Long examId, Long questionId, String currentTime) {
        return Result.success(userQuestionService.exeResult(examId, questionId, currentTime));
    }
}

package com.viewer.friend.controller.user;

import com.viewer.common.core.constants.HttpConstants;
import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.friend.Service.user.IUserExamService;
import com.viewer.friend.aspect.CheckUserStatus;
import com.viewer.friend.domain.exam.dto.ExamDTO;
import com.viewer.friend.domain.exam.dto.ExamQueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/user/exam")
public class UserExamController extends BaseController {
    @Autowired
    private IUserExamService userExamService;

    @CheckUserStatus
    @PostMapping("/enter")
    public Result<Void> enter(@RequestHeader(HttpConstants.AUTHENTICATION) String token, @RequestBody ExamDTO examDTO) {
        return getResult(userExamService.enter(token, examDTO.getExamId()));
    }

    @GetMapping("/list")
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        return userExamService.list(examQueryDTO);
    }
}

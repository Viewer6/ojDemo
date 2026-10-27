package com.viewer.friend.controller.user;


import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.PageQueryDTO;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.friend.Service.user.IUserMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/message")
public class UserMessageController extends BaseController {

    @Autowired
    private IUserMessageService userMessageService;

    @GetMapping("/list")
    public TableDataInfo list(PageQueryDTO dto) {
        return userMessageService.list(dto);
    }
}

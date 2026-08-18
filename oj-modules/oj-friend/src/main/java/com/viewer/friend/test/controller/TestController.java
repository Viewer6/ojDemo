package com.viewer.friend.test.controller;


import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.Result;
import com.viewer.common.message.service.AliSmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController extends BaseController {

    @Autowired
    private AliSmsService aliSmsService;

    @GetMapping("/sendCode")
    public Result<Void> sendCode(String phone, String code) {
        log.info("验证码发送测试, {}, {}", phone, code);
        return getResult(aliSmsService.sendMobileCode(phone, code));
    }
}

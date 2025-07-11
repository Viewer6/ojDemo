package com.viewer.friend.controller;

import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.Result;
import com.viewer.friend.Service.IUserService;
import com.viewer.friend.domain.dot.UserDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Resource(name = "userServiceImpl")
    private IUserService userService;

    @PostMapping("/sendCode")
    public Result<Void> sendCode(@RequestBody UserDTO userDTO){
        return getResult(userService.sendCode(userDTO));
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody UserDTO userDTO){
        return userService.login(userDTO);
    }
}

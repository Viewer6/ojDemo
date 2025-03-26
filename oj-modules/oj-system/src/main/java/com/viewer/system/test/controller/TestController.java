package com.viewer.system.test.controller;

import com.viewer.common.core.domain.Result;
import com.viewer.common.redis.service.RedisService;
import com.viewer.system.domain.SysUser;
import com.viewer.system.test.service.ITestService;
import com.viewer.system.test.service.impl.TestServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {
    @Resource(name = "testServiceImpl")
    private TestServiceImpl testService;

    @Resource
    private RedisService redisService;

    @RequestMapping("/list")
    public List<?> list(){
        return testService.list();
    }

    @RequestMapping("/redisTest")
    public Result<String> redisTest(){
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount("redisTest");
        redisService.setCacheObject("user", sysUser);

        SysUser us = redisService.getCacheObject("user", SysUser.class);
        return Result.success(us.toString());
    }
}

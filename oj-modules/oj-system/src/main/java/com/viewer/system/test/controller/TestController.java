package com.viewer.system.test.controller;

import com.viewer.system.test.service.ITestService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource(name = "TestServiceImpl")
    private ITestService testService;

    public List<?> list(){
        return testService.list();
    }
}

package com.viewer.system.test.controller;

import com.viewer.system.test.service.ITestService;
import com.viewer.system.test.service.impl.TestServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource(name = "testServiceImpl")
    private TestServiceImpl testService;

    @RequestMapping("/list")
    public List<?> list(){
        return testService.list();
    }
}

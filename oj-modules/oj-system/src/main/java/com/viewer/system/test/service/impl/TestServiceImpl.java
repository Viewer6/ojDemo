package com.viewer.system.test.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.viewer.system.test.commen.Domain.TestDomain;
import com.viewer.system.test.mapper.TestMapper;
import com.viewer.system.test.service.ITestService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestServiceImpl implements ITestService {

    @Resource(name = "testMapper")
    private TestMapper testMapper;
    @Override
    public List<?> list() {
        return testMapper.selectList(new QueryWrapper<TestDomain>());
    }
}

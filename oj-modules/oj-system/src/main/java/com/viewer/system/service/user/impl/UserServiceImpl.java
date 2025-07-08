package com.viewer.system.service.user.impl;

import com.github.pagehelper.PageHelper;
import com.viewer.common.core.emuns.ResultCode;
import com.viewer.common.core.exception.UserException;
import com.viewer.system.domain.user.User;
import com.viewer.system.domain.user.dto.UserDTO;
import com.viewer.system.domain.user.dto.UserQueryDTO;
import com.viewer.system.domain.user.vo.UserVO;
import com.viewer.system.mapper.user.UserMapper;
import com.viewer.system.service.user.IUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Override
    public List<UserVO> list(UserQueryDTO userQueryDTO) {
        PageHelper.startPage(userQueryDTO.getPageNum(), userQueryDTO.getPageSize());
        return userMapper.selectUserList(userQueryDTO);
    }

    @Override
    public int updateStatus(UserDTO userDTO) {
        User user = userMapper.selectById(userDTO.getUserId());
        if(user == null){
            throw new UserException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setStatus(userDTO.getStatus());
        return userMapper.updateById(user);
    }
}

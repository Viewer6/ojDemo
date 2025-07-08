package com.viewer.system.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viewer.system.domain.user.User;
import com.viewer.system.domain.user.dto.UserQueryDTO;
import com.viewer.system.domain.user.vo.UserVO;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    List<UserVO> selectUserList(UserQueryDTO userQueryDTO);
}

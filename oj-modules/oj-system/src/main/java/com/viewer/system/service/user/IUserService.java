package com.viewer.system.service.user;

import com.viewer.system.domain.user.dto.UserDTO;
import com.viewer.system.domain.user.dto.UserQueryDTO;
import com.viewer.system.domain.user.vo.UserVO;

import java.util.List;

public interface IUserService {
    List<UserVO> list(UserQueryDTO userQueryDTO);

    int updateStatus(UserDTO userDTO);
}

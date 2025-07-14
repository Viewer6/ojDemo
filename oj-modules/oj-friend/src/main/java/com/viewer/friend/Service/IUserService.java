package com.viewer.friend.Service;

import com.viewer.common.core.domain.Result;
import com.viewer.friend.domain.dot.UserDTO;
import com.viewer.friend.domain.vo.UserInfoVO;

public interface IUserService {
    boolean sendCode(UserDTO userDTO);

    Result<String> login(UserDTO userDTO);

    boolean logout(String token);

    Result<UserInfoVO> getUserInfo(String token);
}

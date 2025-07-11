package com.viewer.friend.Service;

import com.viewer.common.core.domain.Result;
import com.viewer.friend.domain.dot.UserDTO;

public interface IUserService {
    boolean sendCode(UserDTO userDTO);

    Result<String> login(UserDTO userDTO);
}

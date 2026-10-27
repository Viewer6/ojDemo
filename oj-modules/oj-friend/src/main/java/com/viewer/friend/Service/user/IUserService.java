package com.viewer.friend.Service.user;

import com.viewer.common.core.domain.Result;
import com.viewer.friend.domain.user.dot.UserDTO;
import com.viewer.friend.domain.user.dot.UserUpdateDTO;
import com.viewer.friend.domain.user.vo.UserInfoVO;
import com.viewer.friend.domain.user.vo.UserVO;

public interface IUserService {
    boolean sendCode(UserDTO userDTO);

    Result<String> login(UserDTO userDTO);

    boolean logout(String token);

    Result<UserInfoVO> getUserInfo(String token);

    UserVO detail();

    int edit(UserUpdateDTO userUpdateDTO);

    int updateHeadImage(String headImage);
}

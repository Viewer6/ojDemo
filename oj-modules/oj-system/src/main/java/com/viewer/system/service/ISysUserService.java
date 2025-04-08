package com.viewer.system.service;

import com.viewer.common.core.domain.Result;
import com.viewer.common.core.domain.vo.LoginUserIdVO;

public interface ISysUserService {
    Result<String> login(String userAccount, String password);

    Result<Void> add(String userAccount, String password);

    Result<Boolean> delete(Long userId);

    Result<LoginUserIdVO> getLoginIdentity(String token);
}

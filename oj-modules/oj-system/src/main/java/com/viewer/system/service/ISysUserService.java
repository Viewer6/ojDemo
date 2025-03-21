package com.viewer.system.service;

import com.viewer.common.core.domain.Result;

public interface ISysUserService {
    Result<Boolean> login(String username, String password);

    Result<Boolean> add(String userAccount, String password);

    Result<Boolean> delete(Long userId);
}

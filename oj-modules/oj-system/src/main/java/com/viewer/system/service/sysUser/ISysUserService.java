package com.viewer.system.service.sysUser;

import com.viewer.common.core.domain.Result;
import com.viewer.common.core.domain.vo.LoginUserIdVO;

public interface ISysUserService {
    Result<String> login(String userAccount, String password);

    Integer add(String userAccount, String password);

    Result<Boolean> delete(Long userId);

    Result<LoginUserIdVO> getLoginIdentity(String token);

    Boolean logout(String token);
}

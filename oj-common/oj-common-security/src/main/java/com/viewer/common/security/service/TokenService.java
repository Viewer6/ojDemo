package com.viewer.common.security.service;

import cn.hutool.core.lang.UUID;
import com.viewer.common.core.constants.CacheConstants;
import com.viewer.common.core.constants.JwtConstants;
import com.viewer.common.redis.service.RedisService;
import com.viewer.common.core.domain.LoginUser;
import com.viewer.common.core.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {
    @Resource
    private RedisService redisService;

    public String getToken(Long userId, String secret, Integer identity){
        String userKey = UUID.fastUUID().toString();
        Map<String, Object> claim = new HashMap<>();
        claim.put(JwtConstants.LOGIN_USER_ID, userId);
        claim.put(JwtConstants.LOGIN_USER_KEY, userKey);
        String token = JwtUtils.createToken(claim, secret);

        String key = CacheConstants.LOGIN_TOKEN_KEY + userKey;
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity);

        redisService.setCacheObject(key, loginUser, CacheConstants.EXP, TimeUnit.MINUTES);

        return token;
    }
}

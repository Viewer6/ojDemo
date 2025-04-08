package com.viewer.common.security.service;

import cn.hutool.core.lang.UUID;
import com.viewer.common.core.constants.CacheConstants;
import com.viewer.common.core.constants.JwtConstants;
import com.viewer.common.core.domain.vo.LoginUserIdVO;
import com.viewer.common.redis.service.RedisService;
import com.viewer.common.core.domain.LoginUser;
import com.viewer.common.core.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TokenService {
    @Resource
    private RedisService redisService;

    public String getToken(Long userId, String secret, Integer identity, String nickName){
        String userKey = UUID.fastUUID().toString();
        Map<String, Object> claim = new HashMap<>();
        claim.put(JwtConstants.LOGIN_USER_ID, userId);
        claim.put(JwtConstants.LOGIN_USER_KEY, userKey);
        String token = JwtUtils.createToken(claim, secret);

        String key = getTokenKey(userKey);
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity);
        loginUser.setNickName(nickName);

        redisService.setCacheObject(key, loginUser, CacheConstants.EXP, TimeUnit.MINUTES);

        return token;
    }

    public void extendToken(String token, String secret){
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret);
            if (claims == null) {
                  log.error("延长token有效时间失败! 解析token异常. token: {}", token);
                return;
            }
        } catch (Exception e) {
            log.error("延长token有效时间失败! 解析token异常. token: {}", token, e);
            return;
        }
        String userKey = JwtUtils.getUserKey(claims); //获取jwt中的key
        String tokenKey = getTokenKey(userKey);
        // 获取过期时间
        Long expire = redisService.getExpire(tokenKey, TimeUnit.MINUTES);
        if(expire != null && expire < CacheConstants.REFRESH_TIME){
            redisService.expire(tokenKey, CacheConstants.EXP, TimeUnit.MINUTES);
        }
    }


    public LoginUser getIdentity(String token, String secret) {
        String key = JwtUtils.getUserKey(JwtUtils.parseToken(token, secret));
        if(key == null){
            return null;
        }
        return redisService.getCacheObject(getTokenKey(key), LoginUser.class);
    }

    private String getTokenKey(String userKey){
        return CacheConstants.LOGIN_TOKEN_KEY + userKey;
    }

}

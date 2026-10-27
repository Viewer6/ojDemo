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

    public String getToken(Long userId, String secret, Integer identity, String nickName, String headImage){
        String userKey = UUID.fastUUID().toString();
        Map<String, Object> claim = new HashMap<>();
        claim.put(JwtConstants.LOGIN_USER_ID, userId);
        claim.put(JwtConstants.LOGIN_USER_KEY, userKey);
        String token = JwtUtils.createToken(claim, secret);

        String key = getTokenKey(userKey);
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity);
        loginUser.setNickName(nickName);
        loginUser.setHeadImage(headImage);

        redisService.setCacheObject(key, loginUser, CacheConstants.EXP, TimeUnit.MINUTES);

        return token;
    }

    public void extendToken(Claims claims){
        String userKey = getUserKey(claims);
        if (userKey == null) {
            return;
        }
        String tokenKey = getTokenKey(userKey);

        //720min  12个小时      剩余  180min 时候对它进行延长
        Long expire = redisService.getExpire(tokenKey, TimeUnit.MINUTES);
        if (expire != null && expire < CacheConstants.REFRESH_TIME) {
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

    public boolean logout(String token, String secret) {
        String key = JwtUtils.getUserKey(JwtUtils.parseToken(token, secret));
        if(key == null){
            return false;
        }
        return redisService.deleteObject(getTokenKey(key));
    }

    public LoginUser getLoginUser(String token, String secret) {
        String userKey = getUserKey(token, secret);
        if (userKey == null) {
            return null;
        }
        return redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);
    }


    public boolean deleteLoginUser(String token, String secret) {
        String userKey = getUserKey(token, secret);
        if (userKey == null) {
            return false;
        }
        return redisService.deleteObject(getTokenKey(userKey));
    }

    public Long getUserId(Claims claims) {
        if (claims == null) return null;
        return Long.valueOf(JwtUtils.getUserId(claims));  //获取jwt中的key
    }

    public String getUserKey(Claims claims) {
        if (claims == null) return null;
        return JwtUtils.getUserKey(claims);  //获取jwt中的key
    }

    private String getUserKey(String token, String secret) {
        Claims claims = getClaims(token, secret);
        if (claims == null) return null;
        return JwtUtils.getUserKey(claims);  //获取jwt中的key
    }

    public Claims getClaims(String token, String secret) {
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret); //获取令牌中信息  解析payload中信息  存储着用户唯一标识信息
            if (claims == null) {
                log.error("解析token：{}, 出现异常", token);
                return null;
            }
        } catch (Exception e) {
            log.error("解析token：{}, 出现异常", token, e);
            return null;
        }
        return claims;
    }

    public void refreshLoginUser(String nickName, String headImage, String userKey) {
        String tokenKey = getTokenKey(userKey);
        LoginUser loginUser = redisService.getCacheObject(tokenKey, LoginUser.class);
        loginUser.setNickName(nickName);
        loginUser.setHeadImage(headImage);
        redisService.setCacheObject(tokenKey, loginUser);
    }

    private String getTokenKey(String userKey){
        return CacheConstants.LOGIN_TOKEN_KEY + userKey;
    }



}

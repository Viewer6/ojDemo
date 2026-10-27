package com.viewer.common.security.interceptor;

import cn.hutool.core.util.StrUtil;
import com.viewer.common.core.constants.Constants;
import com.viewer.common.core.constants.HttpConstants;
import com.viewer.common.core.utils.ThreadLocalUtil;
import com.viewer.common.security.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Value("${jwt.secret}")
    private String secret; // 这个值从哪个容器拿取决于从哪个微服务调用
    @Resource
    private TokenService tokenService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = getToken(request);
        if(token == null || token.isBlank()){
            // 无token，直接放行；
            return true;
        }
        Claims claims = tokenService.getClaims(token, secret);
        Long userId = tokenService.getUserId(claims);
        String userKey = tokenService.getUserKey(claims);
        ThreadLocalUtil.set(Constants.USER_ID, userId);
        ThreadLocalUtil.set(Constants.USER_KEY, userKey);

        tokenService.extendToken(claims);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }

    /**
     * 从请求头中获取token
     * @param request 请求头
     * @return token
     */
    private String getToken(HttpServletRequest request){
        String token = request.getHeader(HttpConstants.AUTHENTICATION);
        if(StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)){
            token = token.replaceFirst(HttpConstants.PREFIX, "");
        }
        return token;
    }
}

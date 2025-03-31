package com.viewer.common.security.interceptor;

import cn.hutool.core.util.StrUtil;
import com.viewer.common.core.constants.HttpConstants;
import com.viewer.common.security.service.TokenService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
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
        tokenService.extendToken(token, secret);
        return true;
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

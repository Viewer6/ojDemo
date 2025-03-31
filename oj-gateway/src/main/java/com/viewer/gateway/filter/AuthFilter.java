package com.viewer.gateway.filter;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.viewer.common.core.constants.CacheConstants;
import com.viewer.common.core.constants.HttpConstants;
import com.viewer.common.core.domain.LoginUser;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.emuns.ResultCode;
import com.viewer.common.core.emuns.UserIdentity;
import com.viewer.common.core.utils.JwtUtils;
import com.viewer.common.redis.service.RedisService;
import com.viewer.gateway.properties.IgnoreWhiteProperties;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;


/**
 * ⽹关鉴权
 * 全局过滤器
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    // 排除过滤的 uri ⽩名单地址，在nacos⾃⾏添加
    @Autowired
    private IgnoreWhiteProperties ignoreWhite;
    @Value("${jwt.secret}")
    private String secret;
    @Autowired
    private RedisService redisService;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath(); // 获取请求的路径, 用于判断接口是否需要进行身份认证
        // 跳过不需要验证的路径
        if (matches(url, ignoreWhite.getWhites())) {
            return chain.filter(exchange);
        }
        //从http请求头中获取token
        String token = getToken(request);
        // 判断令牌是否为空, 若为空则token过期或者用户未登录
        if (StrUtil.isEmpty(token)) {
            return unauthorizedResponse(exchange, "令牌不能为空");
        }
        // 解析令牌中的信息
        Claims claims;
        try {
            // 获取令牌中信息 解析payload中信息
            // 基于webflux
            claims = JwtUtils.parseToken(token, secret);
            if (claims == null) {
                return unauthorizedResponse(exchange, "令牌已过期或验证不正确！");
            }
        } catch (Exception e) {
            return unauthorizedResponse(exchange, "令牌已过期或验证不正确！");
        }

        // 解析claim, 校验令牌
        String userKey = JwtUtils.getUserKey(claims); //获取jwt中的key
        boolean isLogin = redisService.hasKey(getTokenKey(userKey));
        if (!isLogin) {
            return unauthorizedResponse(exchange, "登录状态已过期");
        }
        String userId = JwtUtils.getUserId(claims); //判断jwt中的信息是否完整
        if (StrUtil.isEmpty(userId)) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }

        // 判断用户身份信息, 管理员or普通用户
        LoginUser user = redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);
        if (url.contains(HttpConstants.SYSTEM_URL_PREFIX) && ! UserIdentity.ADMIN.getValue().equals(user.getIdentity())) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }
        if (url.contains(HttpConstants.FRIEND_URL_PREFIX) && ! UserIdentity.ORDINARY.getValue().equals(user.getIdentity())) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }
        return chain.filter(exchange);
    }

    /**
     * 查找指定url是否匹配指定匹配规则链表中的任意⼀个字符串
     *
     * @param url 指定url
     * @param patternList 需要检查的匹配规则链表(接口白名单)
     * @return 是否匹配
     */
    private boolean matches(String url, List<String> patternList) {
        if (StrUtil.isEmpty(url) || CollectionUtils.isEmpty(patternList)) {
            return false;
        }
        // 遍历白名单中的地址, 如果符合返回true, 否则返回false
        for (String pattern : patternList) {
            if (isMatch(pattern, url)) {
                return true;
            }
        }
        return false;
    }
    /**
     * 判断url是否与规则匹配
     * 匹配规则中：
     * ? 表示单个字符;
     * * 表示一层路径内的任意字符串，不可跨层级;
     * ** 表示任意层路径;
     *
     * @param pattern 匹配规则
     * @param url 需要匹配的url
     * @return 是否匹配
     */
    private boolean isMatch(String pattern, String url) {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }
    /**
     * 获取缓存key
     */
    private String getTokenKey(String token) {
        return CacheConstants.LOGIN_TOKEN_KEY + token;
    }
    /**
     * 从请求头中获取请求token
     */
    private String getToken(ServerHttpRequest request) {
        String token =
                request.getHeaders().getFirst(HttpConstants.AUTHENTICATION);
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StrUtil.isNotEmpty(token) &&
                token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return token;
    }
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg) {
        log.error("[鉴权异常处理]请求路径:{}", exchange.getRequest().getPath());
        return webFluxResponseWriter(exchange.getResponse(), msg, ResultCode.FAILED_UNAUTHORIZED.getCode());
    }
    //拼装webflux模型响应
    private Mono<Void> webFluxResponseWriter(ServerHttpResponse response,
                                             String msg, int code) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_VALUE);
        Result<?> result = Result.fail(code, msg);
        DataBuffer dataBuffer =
                response.bufferFactory().wrap(JSON.toJSONString(result).getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }
    // 配置优先级
    @Override
    public int getOrder() {
        return -200;
    }



}

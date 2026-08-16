package com.viewer.common.core.utils;

import com.viewer.common.core.constants.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    /**
     * 生成令牌
     *
     * @param claim 数据
     * @param key 密钥
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claim, String key){
        String token = Jwts.builder().setClaims(claim).signWith(SignatureAlgorithm.HS512, key).compact();
        return token;
    }

    /**
     * 从令牌里提取信息
     * @param token 令牌
     * @param key 密钥
     * @return 数据
     */
    public static Claims parseToken(String token, String key){
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
    }

    public static String getUserKey(Claims claims) {
        Object value = claims.get(JwtConstants.LOGIN_USER_KEY);
        if (value == null){
            return "";
        }
        return value.toString();
    }

    public static String getUserId(Claims claims) {
        Object value = claims.get(JwtConstants.LOGIN_USER_ID);
        if (value == null){
            return "";
        }
        return value.toString();
    }

    /**
     * 测试
     */
//    private static final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
//    public static void main(String[] args) {
//        String key = "RuKgPbRZIv79sx1oEFahdkxhdnejcjfh9zFZH3gx7xJkknsTq6lDEr8gaR7eL9LZUZz5DHpLFaKnUVap6LUURK0ly08Q4cuUqaA==";
//        Map<String, Object> claim = new HashMap<>();
//        claim.put("userId", 12345678L);
//        String token = createToken(claim, key);
//        System.out.println("token:                   "+token);
//        System.out.println();
//        System.out.println(parseToken(token, key));
//        System.out.println(key);
//    }
}

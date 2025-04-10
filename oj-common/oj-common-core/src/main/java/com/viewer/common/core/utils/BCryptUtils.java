package com.viewer.common.core.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// BCrypt加密算法
public class BCryptUtils {
    /**
     * 密码加密
     * @param password 密码
     * @return 加密后的字符串
     */
    public static String encryptPassword(String password){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 校验密码
     * @param rawPassword 输入的密码
     * @param encodedPassword 加密后数据库存储的密码
     * @return 校验结果
     */
    public static Boolean matchesPassword(String rawPassword, String encodedPassword){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}

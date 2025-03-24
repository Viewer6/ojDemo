package com.viewer.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.emuns.ResultCode;
import com.viewer.common.core.emuns.UserIdentity;
import com.viewer.common.security.service.TokenService;
import com.viewer.system.domain.SysUser;
import com.viewer.system.mapper.SysUserMapper;
import com.viewer.system.service.ISysUserService;
import com.viewer.system.utils.BCryptUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SysUserServiceImpl implements ISysUserService {

    @Resource(name = "sysUserMapper")
    private SysUserMapper sysUserMapper;

    @Resource
    private TokenService tokenService;

    @Value("${jwt.secret}")
    private String secret; // 密钥

    @Override
    public Result<String> login(String username, String password) {
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserAccount, username));
        if (sysUser == null){
            return Result.fail(ResultCode.FAILED_USER_NOT_EXISTS, null);
        }
        if(!BCryptUtils.matchesPassword(password, sysUser.getPassword())){
            return Result.fail(ResultCode.FAILED_LOGIN, null);
        }
        String token = tokenService.getToken(sysUser.getUserId(), secret, UserIdentity.ADMIN.getValue());
        return Result.success(token);
    }

    @Override
    public Result<Boolean> add(String userAccount, String password) {
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount(userAccount);
        sysUser.setPassword(BCryptUtils.encryptPassword(password));
        try {
            sysUserMapper.insert(sysUser);
        }catch (Exception e){
            log.error("添加失败! e: ", e.fillInStackTrace());
            return Result.fail(ResultCode.FAILED, false);
        }
        return Result.success(true);
    }

    @Override
    public Result<Boolean> delete(Long userId) {
         int result = sysUserMapper.delete(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserId, userId)
        );
         if (result == 0){
             return Result.fail(ResultCode.FAILED_USER_NOT_EXISTS, false);
         }
         return Result.success(true);
    }
}

package com.viewer.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.viewer.common.core.constants.HttpConstants;
import com.viewer.common.core.domain.LoginUser;
import com.viewer.common.core.domain.vo.LoginUserIdVO;
import com.viewer.common.core.service.BaseService;
import com.viewer.common.core.domain.BaseEntity;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.emuns.ResultCode;
import com.viewer.common.core.emuns.UserIdentity;
import com.viewer.common.redis.service.RedisService;
import com.viewer.common.security.service.TokenService;
import com.viewer.system.domain.SysUser;
import com.viewer.system.mapper.SysUserMapper;
import com.viewer.system.service.ISysUserService;
import com.viewer.system.utils.BCryptUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SysUserServiceImpl extends BaseService implements ISysUserService{

    @Resource(name = "sysUserMapper")
    private SysUserMapper sysUserMapper;

    @Resource
    private TokenService tokenService;

    @Value("${jwt.secret}")
    private String secret; // 密钥

    @Resource
    private RedisService redisService;

    @Override
    public Result<String> login(String userAccount, String password) {
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserAccount, userAccount));
        if (sysUser == null){
            return Result.fail(ResultCode.FAILED_USER_NOT_EXISTS, null);
        }
        if(!BCryptUtils.matchesPassword(password, sysUser.getPassword())){
            return Result.fail(ResultCode.FAILED_LOGIN, null);
        }
        String token = tokenService.getToken(sysUser.getUserId(), secret, UserIdentity.ADMIN.getValue(), sysUser.getNickName());
        return Result.success(token);
    }

    @Override
    public Result<Void> logout(String token) {
        if(StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)){
            token = token.replaceFirst(HttpConstants.PREFIX, "");
        }
        return getResult(tokenService.logout(token, secret));
    }

    @Override
    public Result<Void> add(String userAccount, String password) {
        List<SysUser> s = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserAccount, userAccount));
        if(CollectionUtil.isNotEmpty(s)){
            return Result.fail(ResultCode.AILED_USER_EXISTS);
        }
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount(userAccount);
        sysUser.setPassword(BCryptUtils.encryptPassword(password));
        return getResult(sysUserMapper.insert(sysUser));
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

    @Override
    public Result<LoginUserIdVO> getLoginIdentity(String token) {
        if(StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)){
            token = token.replaceFirst(HttpConstants.PREFIX, "");
        }
        LoginUser loginUser = tokenService.getIdentity(token, secret);
        if(loginUser == null){
            return Result.fail(null);
        }
        return Result.success(new LoginUserIdVO(loginUser.getNickName()));
    }


}

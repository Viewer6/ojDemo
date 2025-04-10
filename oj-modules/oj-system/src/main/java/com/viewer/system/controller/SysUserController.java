package com.viewer.system.controller;


import com.viewer.common.core.constants.HttpConstants;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.domain.vo.LoginUserIdVO;
import com.viewer.system.domain.sysUser.dto.LoginDTO;
import com.viewer.system.domain.sysUser.dto.SysUserSaveDTO;
import com.viewer.system.domain.sysUser.vo.SysUserVO;
import com.viewer.system.service.sysUser.impl.SysUserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Tag注解
 * 介绍: 用于给接口分组, 用途类似于为接口文档添加标签
 * 用于: 方法, 类, 接口
 */
//@CrossOrigin(origins = "http://localhost:5174")
@Tag(name = "管理员用户API")
@Slf4j
@RestController
@RequestMapping("/sysUser")
public class SysUserController{

    @Resource(name = "sysUserServiceImpl")
    private SysUserServiceImpl sysUserService;

    /**
     * @Operation注解
     * 介绍: 用于描述接口的操作
     * 常用属性: summary操作的摘要信息; description操作的详情信息
     *
     * @ApiResponse注解
     * 介绍: 用于描述操作的响应结果
     * 常用属性: responseCode响应的状态码; description响应的描述
     */
    @Operation(summary = "新增管理员", description = "根据提供的信息新增管理员用户")
    @ApiResponse(responseCode = "1000", description = "操作成功")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "用户已存在")
    @PostMapping("/addSysUser")
    public Result<Void> addSysUser(@RequestBody SysUserSaveDTO sysUserSaveDTO){
        log.info("添加的账户名称: {}; 账户密码: {}", sysUserSaveDTO.getUserAccount(), sysUserSaveDTO.getPassword());
        return sysUserService.add(sysUserSaveDTO.getUserAccount(), sysUserSaveDTO.getPassword());
    }

    @DeleteMapping("/userId")
    @Operation(summary = "删除用户", description = "通过用户id删除用户")
    @Parameters(value = {
            @Parameter(name = "userId", in = ParameterIn.PATH, description = "用户ID")
    })
    @ApiResponse(responseCode = "1000", description = "成功删除用户")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3102", description = "用户不存在")
    public Result<Boolean> deleteSysUser(@PathVariable @NotNull Long userId){
        log.info("要删除的管理员id: {}", userId);
        return sysUserService.delete(userId);
    }

    @Operation(summary = "用户详情", description = "根据查询条件查询用户详情")
    @GetMapping("/detail")
    @Parameters(value = {
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "用户ID"),
            @Parameter(name = "sex", in = ParameterIn.QUERY, description = "用户性别")
    })
    @ApiResponse(responseCode = "1000", description = "成功获取用户信息")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "用户不存在")
    public Result<SysUserVO> detail(Long userId, @RequestParam(required = false) String sex) {
        return null;
    }


    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "通过用户账号和密码登录")
    @ApiResponse(responseCode = "1000", description = "成功登录")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3102", description = "用户不存在")
    @ApiResponse(responseCode = "3103", description = "用户名或密码错误")
    @ApiResponse(responseCode = "3104", description = "您已被列⼊⿊名单, 请联系管理员")
    public Result<String> login(@RequestBody LoginDTO loginDTO){
        log.info("登录的用户名: {}; 登录的密码: {}", loginDTO.getUserAccount(), loginDTO.getPassword());
        return sysUserService.login(loginDTO.getUserAccount(), loginDTO.getPassword());
    }

    @DeleteMapping("/logout")
    public  Result<Void> logout(@RequestHeader(HttpConstants.AUTHENTICATION) String token){
        return sysUserService.logout(token);
    }

    @GetMapping("/getLoginIdentity")
    public Result<LoginUserIdVO> getLoginUserIdentity(@RequestHeader(HttpConstants.AUTHENTICATION) String token){
//        log.info(token.substring(7));
        return sysUserService.getLoginIdentity(token); // .substring(7)
    }
}

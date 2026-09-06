package com.viewer.friend.Service.user.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.viewer.common.core.constants.CacheConstants;
import com.viewer.common.core.constants.Constants;
import com.viewer.common.core.constants.HttpConstants;
import com.viewer.common.core.domain.LoginUser;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.emuns.ResultCode;
import com.viewer.common.core.emuns.UserIdentity;
import com.viewer.common.core.emuns.UserStatus;
import com.viewer.common.core.exception.UserException;
import com.viewer.common.message.service.AliSmsService;
import com.viewer.common.redis.service.RedisService;
import com.viewer.common.security.service.TokenService;
import com.viewer.friend.Service.user.IUserService;
import com.viewer.friend.domain.user.User;
import com.viewer.friend.domain.user.dot.UserDTO;
import com.viewer.friend.domain.user.vo.UserInfoVO;
import com.viewer.friend.mapper.user.UserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AliSmsService aliSmsService;

    @Value("${sms.code-expiration}")
    private Long phoneCodeExpiration; // 验证码时效

    @Value("${sms.send-limit}")
    private Integer sendLimit; // 发送次数限制

    @Value("${jwt.secret}")
    private String secret; // 密钥

    @Value("${sms.is-send}")
    private boolean isSend;  //开关打开：true  开关关闭false

    @Override
    public boolean sendCode(UserDTO userDTO) {
        if(!checkPhone(userDTO.getPhone())){
            throw new UserException(ResultCode.FAILED_USER_PHONE);
        }

        String codeTimesKey = getCodeTimesKey(userDTO.getPhone());
        String phoneCodeKey = getPhoneCodeKey(userDTO.getPhone());


        // 1分钟内不能频繁获取验证码
        Long expire = redisService.getExpire(phoneCodeKey, TimeUnit.SECONDS);
        if (expire != null && (phoneCodeExpiration * 60 - expire) < 1 ){
            throw new UserException(ResultCode.FAILED_FREQUENT);
        }


        String code = isSend ? RandomUtil.randomNumbers(6) : Constants.DEFAULT_CODE;  // 生成6位验证码 : 默认密码：123456
        redisService.setCacheObject(phoneCodeKey, code, phoneCodeExpiration, TimeUnit.MINUTES);

        // 发送次数判定
        Integer sendTimes = redisService.getCacheObject(codeTimesKey, Integer.class);
        if(sendTimes != null && sendTimes >= sendLimit){
            throw new UserException(ResultCode.FAILED_TIME_LIMIT);
        }

        if (isSend) {
            // todo: 发送验证码服务
            aliSmsService.sendMobileCode(userDTO.getPhone(), code);
            log.info("验证码: {}", code);
            redisService.increment(codeTimesKey);
        }

        if (sendTimes == null) {  //说明是当天第一次发起获取验证码的请求
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            redisService.expire(codeTimesKey, seconds, TimeUnit.SECONDS);
        }

        return true;
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        checkCode(userDTO.getPhone(), userDTO.getCode());

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, userDTO.getPhone()));
        if(user == null){ // 首次登录(注册)
            String nickName = RandomUtil.randomString(6);

            user = new User();
            user.setPhone(userDTO.getPhone());
            user.setStatus(UserStatus.NORMAL.getValue());
            user.setHeadImage(Constants.DEFAULT_HEAD_IMAGE);
            user.setNickName(nickName);
            userMapper.insert(user);
        }

        String token = tokenService.getToken(user.getUserId(), secret, UserIdentity.ORDINARY.getValue(), user.getNickName(), user.getHeadImage());
        return Result.success(token);
    }

    @Override
    public boolean logout(String token) {
        if(StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)){
            token = token.replaceFirst(HttpConstants.PREFIX, "");
        }
        return tokenService.logout(token, secret);
    }

    @Override
    public Result<UserInfoVO> getUserInfo(String token) {
        if(StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)){
            token = token.replaceFirst(HttpConstants.PREFIX, "");
        }
        LoginUser loginUser = tokenService.getIdentity(token, secret);
        if(loginUser == null){
            return Result.fail(null);
        }
        return Result.success(new UserInfoVO(loginUser.getNickName(), loginUser.getHeadImage()));
    }

    private String getCodeTimesKey(String phone) {
        return CacheConstants.CODE_TIME_KEY + phone;
    }

    private String getPhoneCodeKey(String phone) {
        return CacheConstants.PHONE_CODE_KEY + phone;
    }

    /**
     * 判断电话号码是否正确
     * @param phone
     * @return
     */
    private boolean checkPhone(String phone) {
        Pattern regex = Pattern.compile("^1[2|3|4|5|6|7|8|9][0-9]\\d{8}$");
        Matcher m = regex.matcher(phone);
        return m.matches();
    }

    /**
     * 判断验证码是否正确
     * @param phone
     * @param code
     */
    private void checkCode(String phone, String code) {
        String phoneCodeKey = getPhoneCodeKey(phone);
        String codeCatch = redisService.getCacheObject(phoneCodeKey, String.class);
        if (StrUtil.isEmpty(codeCatch)) {
            throw new UserException(ResultCode.FAILED_INVALID_CODE);
        }
        if(!codeCatch.equals(code)){
            throw new UserException(ResultCode.FAILED_CODE);
        }
        redisService.deleteObject(phoneCodeKey);
    }
}

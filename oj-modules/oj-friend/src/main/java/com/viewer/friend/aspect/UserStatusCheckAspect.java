package com.viewer.friend.aspect;

import com.viewer.common.core.constants.Constants;
import com.viewer.common.core.enums.ResultCode;
import com.viewer.common.core.exception.ExamException;
import com.viewer.common.core.utils.ThreadLocalUtil;
import com.viewer.friend.domain.user.vo.UserVO;
import com.viewer.friend.manager.UserCacheManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
public class UserStatusCheckAspect {

    @Autowired
    private UserCacheManager userCacheManager;

    @Before(value = "@annotation(com.viewer.friend.aspect.CheckUserStatus)")
    public void before(JoinPoint point){
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        UserVO user = userCacheManager.getUserById(userId);
        if (user == null) {
            throw new ExamException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if (Objects.equals(user.getStatus(), Constants.FALSE)) {
            throw new ExamException(ResultCode.FAILED_USER_BANNED);
        }
    }
}

package org.example.ojfriend.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.common.core.constants.Constants;
import org.example.common.core.enums.ResultCode;
import org.example.common.core.utils.ThreadLocalUtil;
import org.example.ojfriend.domain.user.vo.UserVO;
import org.example.ojfriend.manager.UserCacheManager;
import org.example.security.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
public class UserStatusCheckAspect {

    @Autowired
    private UserCacheManager userCacheManager;

    @Before(value = "@annotation(org.example.ojfriend.aspect.CheckUserStatus)")
    public void before(JoinPoint point){
        //获取用户id
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        //检测用户是否存在
        UserVO user = userCacheManager.getUserById(userId);
        if (user == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        //校验用户是否被禁用
        if (Objects.equals(user.getStatus(), Constants.FALSE)) {
            throw new ServiceException(ResultCode.FAILED_USER_BANNED);
        }
    }
}

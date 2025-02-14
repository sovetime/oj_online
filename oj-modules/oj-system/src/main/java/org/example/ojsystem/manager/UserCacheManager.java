package org.example.ojsystem.manager;

import org.example.common.core.constants.CacheConstants;
import org.example.ojsystem.domain.user.User;
import org.example.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserCacheManager {

    @Autowired
    private RedisService redisService;

    //更新用户缓存信息
    public void updateStatus(Long userId, Integer status) {
        String userKey = getUserKey(userId);
        //从缓存中获取用户信息
        User user = redisService.getCacheObject(userKey, User.class);
        if (user == null) {
            return;
        }
        user.setStatus(status);
        redisService.setCacheObject(userKey, user);
        //设置用户缓存有效期为10分钟
        redisService.expire(userKey, CacheConstants.USER_EXP, TimeUnit.MINUTES);
    }

    private String getUserKey(Long userId) {
        return CacheConstants.USER_DETAIL + userId;
    }
}

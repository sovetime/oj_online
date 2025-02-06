package org.example.ojfriend.manager;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import org.example.common.core.constants.CacheConstants;
import org.example.ojfriend.domain.user.User;
import org.example.ojfriend.domain.user.vo.UserVO;
import org.example.ojfriend.mapper.user.UserMapper;
import org.example.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserCacheManager {

    @Autowired
    private RedisService redisService;
    @Autowired
    private UserMapper userMapper;

    //根据用户id获取用户信息
    public UserVO getUserById(Long userId) {
        String userKey = getUserKey(userId);
        UserVO userVO = redisService.getCacheObject(userKey, UserVO.class);
        if (userVO != null) {
            //将缓存延长10min
            redisService.expire(userKey, CacheConstants.USER_EXP, TimeUnit.MINUTES);
            return userVO;
        }

        //缓存中没有数据，从数据库中查询
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getUserId,
                        User::getNickName,
                        User::getHeadImage,
                        User::getSex,
                        User::getEmail,
                        User::getPhone,
                        User::getWechat,
                        User::getIntroduce,
                        User::getSchoolName,
                        User::getMajorName,
                        User::getStatus)
                .eq(User::getUserId, userId));
        if (user == null) {
            return null;
        }
        refreshUser(user);

        userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    //刷新用户缓存
    public void refreshUser(User user) {
        String userKey = getUserKey(user.getUserId());
        redisService.setCacheObject(userKey, user);
        //这里给缓存设置过期时间的目的是防止用户短时间内多次修改个人信息
        redisService.expire(userKey, CacheConstants.USER_EXP, TimeUnit.MINUTES);
    }

    //u:d:用户id
    private String getUserKey(Long userId) {
        return CacheConstants.USER_DETAIL + userId;
    }
}

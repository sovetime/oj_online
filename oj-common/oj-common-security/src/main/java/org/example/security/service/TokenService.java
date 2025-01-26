package org.example.security.service;

import cn.hutool.core.lang.UUID;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.example.common.core.constants.CacheConstants;
import org.example.common.core.domain.LoginUser;
import org.example.common.core.utils.JwtUtils;
import org.example.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.example.common.core.constants.CacheConstants.EXPIRATION;
import static org.example.common.core.constants.CacheConstants.LOGIN_TOKEN_KEY;
import static org.example.common.core.constants.JwtConstants.LOGIN_USER_ID;
import static org.example.common.core.constants.JwtConstants.LOGIN_USER_KEY;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-31
 * Time: 14:17
 */

@Service
@Slf4j
public class TokenService {
    @Autowired
    private RedisService redisService;

    public String createToken(Long userId, String secret, Integer identity, String nickName, String headImage) {
        //密码校验成功后，生成jwt token
        Map<String, Object> claims=new HashMap<>();
        //这里使用UUID生成userKey会导致redis/缓存大量的key堆积，浪费资源
        //因为每次的userKey都是需要重新生成的，不是相同的，这作为后面一个优化点
        String userKey = UUID.fastUUID().toString();
        claims.put(LOGIN_USER_ID, userId);
        claims.put(LOGIN_USER_KEY,userKey);
        String token= JwtUtils.createToken(claims, secret);

        //第三方校验机制中存储敏感信息 redis  用户信息identity，1-普通用户 2-管理用户
        //身份认证存储的信息 我们使用key-value结构存储信息
        //key 必须保证唯一，需要统一格式

        //我们要把key存储在缓存中,需要不断检测是否过期，过期需要给用户退出登录状态，返回的时候携带token（中又key)
        //可以和缓存中存储的key校验
        String tokenKey=getTokenKey(userKey);
        LoginUser loginUser=new LoginUser();
        loginUser.setIdentity(identity);
        loginUser.setNickName(nickName);
        loginUser.setHeadImage(headImage);
        //记录jwt的过期时间，这里设置两天
        redisService.setCacheObject(tokenKey,loginUser,EXPIRATION, TimeUnit.MINUTES);
        return token;
    }

    //延长token的有效时间，延长redis当中从存储的用于用户身份认证的敏感信息的有效时间
    //在身份认证通过之后才会调用的，并且在请求到达controller层之前  在拦截器中调用
    public void extendToken(Claims claims) {
        String userKey=getUserKey(claims);//获取jwt中的key
        if(userKey==null){
            return;
        }
        String tokenKey=getTokenKey(userKey);

        //获取缓存中key的剩余时间
        Long expire=redisService.getExpire(tokenKey,TimeUnit.MINUTES);
        //剩余时间小于REFRESH_TIME的时候进行时间更新
        if (expire != null && expire < CacheConstants.REFRESH_TIME) {
            redisService.expire(tokenKey, CacheConstants.EXPIRATION, TimeUnit.MINUTES);
        }
    }
    public String getUserKey(Claims claims) {
        if (claims == null) {
            return null;
        }
        return JwtUtils.getUserKey(claims);  //获取jwt中的key
    }

    //根据tokenkey获取缓存中的用户信息
    public LoginUser getLoginUser(String token,String secret) {
        String userKey = getUserKey(token, secret);//获取jwt中的key
        if (userKey == null) {
            return null;
        }
        return redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);
    }

    //删除redis中存储的token
    public boolean deleteLoginUser(String token, String secret) {
        String userKey = getUserKey(token, secret);//获取jwt中存储的key
        if(userKey==null){
            return false;
        }
        return redisService.deleteObject(getTokenKey(userKey));
    }

    //获取jwt中的key
    private String getUserKey(String token, String secret) {
        Claims claims = getClaims(token, secret);
        if (claims == null) {
            return null;
        }
        return JwtUtils.getUserKey(claims);
    }
    //获取Claims中存储的信息
    public Claims getClaims(String token, String secret) {
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret); //获取令牌中信息  解析payload中信息  存储着用户唯一标识信息
            if (claims == null) {
                log.error("解析token：{}, 出现异常", token);
                return null;
            }
        } catch (Exception e) {
            log.error("解析token：{}, 出现异常", token, e);
            return null;
        }
        return claims;
    }

    //更新用户登录信息
    public void refreshLoginUser(String nickName, String headImage, String userKey) {
        String tokenKey = getTokenKey(userKey);
        LoginUser loginUser = redisService.getCacheObject(tokenKey, LoginUser.class);
        loginUser.setNickName(nickName);
        loginUser.setHeadImage(headImage);
        redisService.setCacheObject(tokenKey, loginUser);
    }

    //获取token中存储的key
    public String getTokenKey(String userKey) {
        return LOGIN_TOKEN_KEY+userKey;
    }

    public Long getUserId(Claims claims) {
        return claims.get(LOGIN_USER_ID, Long.class);
    }


}

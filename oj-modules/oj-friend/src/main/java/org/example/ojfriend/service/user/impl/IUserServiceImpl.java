package org.example.ojfriend.service.user.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.core.constants.CacheConstants;
import org.example.common.core.constants.Constants;
import org.example.common.core.constants.HttpConstants;
import org.example.common.core.domain.LoginUser;
import org.example.common.core.domain.R;
import org.example.common.core.domain.vo.LoginUserVO;
import org.example.common.core.enums.ResultCode;
import org.example.common.core.enums.UserIdentity;
import org.example.common.core.enums.UserStatus;
import org.example.common.core.utils.ThreadLocalUtil;
import org.example.message.service.AliSmsService;
import org.example.ojfriend.domain.user.User;
import org.example.ojfriend.domain.user.dto.UserDTO;
import org.example.ojfriend.domain.user.dto.UserUpdateDTO;
import org.example.ojfriend.domain.user.vo.UserVO;
import org.example.ojfriend.manager.UserCacheManager;
import org.example.ojfriend.mapper.user.UserMapper;
import org.example.ojfriend.service.user.IUserService;
import org.example.redis.service.RedisService;
import org.example.security.exception.ServiceException;
import org.example.security.service.TokenService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-19
 * Time: 12:32
 */
@Service
@RefreshScope
public class IUserServiceImpl implements IUserService {

    @Autowired
    private AliSmsService aliSmsService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserCacheManager userCacheManager;

    //后面设置的值是默认项，如果没有找到相对应的配置可以直接使用配置项,nacos存在对应的配置使用nacos
    @Value("${sms.code-expiration:5}")
    private Long phoneCodeExpiration;
    @Value("${sms.send-limit:50}")
    private Integer sendLimit;
    @Value("${sms.is-send:false}")//我们这里短信默认不发送，默认短信验证码是123456，方便未授权手机号进行登录
    private boolean isSend;
    @Value("${jwt.secret}")
    private String secret;
    @Value("${file.oss.downloadUrl}")
    private String downloadUrl;

    @Override
    public boolean sendCode(UserDTO userDTO) {
        //校验手机号是否正确
        if(!checkPhone(userDTO.getPhone())){
            throw new ServiceException(ResultCode.FAILED_USER_PHONE);
        }

        //获取缓存中存储验证码的key
        String phoneCodeKey = getPhoneCodeKey(userDTO.getPhone());
        //获取验证码剩余有效时间，防止验证码在1分钟内重复发送
        Long expire = redisService.getExpire(phoneCodeKey, TimeUnit.SECONDS);
        if (expire != null && (phoneCodeExpiration * 60 - expire) < 60 ){
            throw new ServiceException(ResultCode.FAILED_FREQUENT);
        }

        //获取缓存中存储获取验证码次数的key
        String codeTimeKey = getCodeTimeKey(userDTO.getPhone());
        //获取已经发送验证码的次数，防止验证码发送次数超过50次
        Long sendTimes = redisService.getCacheObject(codeTimeKey, Long.class);
        if (sendTimes != null && sendTimes >= sendLimit) {
            throw new ServiceException(ResultCode.FAILED_TIME_LIMIT);
        }

        //生成6位验证码
        String code = isSend ? RandomUtil.randomNumbers(6) : Constants.DEFAULT_CODE;
        //缓存验证码
        redisService.setCacheObject(phoneCodeKey, code, phoneCodeExpiration, TimeUnit.MINUTES);
        if(isSend){
            boolean sendMobileCode = aliSmsService.sendMobileCode(userDTO.getPhone(), code);//发送短信验证码
            if(!sendMobileCode){
                throw new ServiceException(ResultCode.FAILED_SEND_CODE);
            }
        }
        redisService.increment(codeTimeKey);//验证码发送次数+1

        //当天第一次发送验证码请求
        if(sendTimes==null){
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),//现在的时间
                    LocalDateTime.now() .plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));//明天0点的时间
            //获取当天的剩余秒数
            redisService.expire(codeTimeKey, seconds, TimeUnit.SECONDS);//设置当天剩余秒数作为过期时间，确保第二天验证码发送次数重置
        }
        return true;
    }

    @Override
    public String codeLogin(String phone, String code) {
        checkCode(phone, code);
        User user=userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        //进行注册
        if(user==null){
            user=new User();
            user.setPhone(phone);
            user.setStatus(UserStatus.Normal.getValue());
            user.setCreateBy(Constants.SYSTEM_USER_ID);//系统操作人员
            userMapper.insert(user);
        }
        return tokenService.createToken(user.getUserId(), secret
                , UserIdentity.ORDINARY.getValue(), user.getNickName(), user.getHeadImage());
    }

    @Override
    public boolean logout(String token) {
        if(StrUtil.isNotEmpty(token)&&token.startsWith(HttpConstants.PREFIX)){
            token=token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        //删除缓存中的token
        return tokenService.deleteLoginUser(token,secret);
    }

    @Override
    public R<LoginUserVO> info(String token) {
        if(StrUtil.isNotEmpty(token)&&token.startsWith(HttpConstants.PREFIX)){
            token=token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }

        LoginUser loginUser=tokenService.getLoginUser(token, secret);
        if(loginUser==null){
            return R.fail();
        }
        LoginUserVO loginUserVO=new LoginUserVO();
        loginUserVO.setNickName(loginUser.getNickName());

        //设置头像
        if (StrUtil.isNotEmpty(loginUser.getHeadImage())) {
            loginUserVO.setHeadImage(downloadUrl + loginUser.getHeadImage());
        }
        return R.ok(loginUserVO);
    }

    @Override
    public UserVO detail() {
        //从ThreadLocal中获取当前线程绑定的用户ID
        Long userId=ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if (userId == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        UserVO userVO = userCacheManager.getUserById(userId);
        if (userVO == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }

        //设置头像
        if (StrUtil.isNotEmpty(userVO.getHeadImage())) {
            userVO.setHeadImage(downloadUrl + userVO.getHeadImage());
        }
        return userVO;
    }

    @Override
    public int edit(UserUpdateDTO userUpdateDTO) {
        //获取userId
        Long userId=ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if (userId == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }

        User user=userMapper.selectById(userId);
        if(user==null){
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        BeanUtils.copyProperties(userUpdateDTO,user);

        //更新用户缓存
        userCacheManager.refreshUser(user);
        tokenService.refreshLoginUser(user.getNickName(),user.getHeadImage(),
                ThreadLocalUtil.get(Constants.USER_KEY, String.class));

        return userMapper.updateById(user);
    }

    @Override
    public int updateHeadImage(String headImage) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if (userId == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setHeadImage(headImage);

        //更新用户缓存
        userCacheManager.refreshUser(user);
        tokenService.refreshLoginUser(user.getNickName(),user.getHeadImage(),
                ThreadLocalUtil.get(Constants.USER_KEY, String.class));
        return userMapper.updateById(user);
    }

    //校验手机号是否正确
    public static boolean checkPhone(String phone) {
        Pattern regex = Pattern.compile("^1[2|3|4|5|6|7|8|9][0-9]\\d{8}$");
        Matcher m = regex.matcher(phone);
        return m.matches();
    }

    //校验验证码是否正确
    private void checkCode(String phone, String code) {
        String phoneCodeKey = getPhoneCodeKey(phone);
        String cacheCode = redisService.getCacheObject(phoneCodeKey, String.class);
        if (StrUtil.isEmpty(cacheCode)) {
            throw new ServiceException(ResultCode.FAILED_INVALID_CODE);
        }
        if (!cacheCode.equals(code)) {
            throw new ServiceException(ResultCode.FAILED_ERROR_CODE);
        }
        //验证码比对成功
        redisService.deleteObject(phoneCodeKey);
    }

    //获取缓存中存储验证码的key
    private String getPhoneCodeKey(String phone) {
        return CacheConstants.PHONE_CODE_KEY + phone;
    }

    //获取缓存中存储获取验证码次数的key
    private String getCodeTimeKey(String phone) {
        return CacheConstants.CODE_TIME_KEY + phone;
    }

}

package org.example.security.interceptor;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.common.core.constants.HttpConstants;
import org.example.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-08
 * Time: 14:07
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    //从哪个服务的配置文件中读取，取决于这个bean对象交给了哪个服务的spring容器进行管理
    @Value("${jwt.secret}")
    private String secret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = getToken(request);  //请求头中获取token
//        if (StrUtil.isEmpty(token)) {
//            return true;
//        }
//        Claims claims = tokenService.getClaims(token, secret);
//        Long userId = tokenService.getUserId(claims);
//        String userKey = tokenService.getUserKey(claims);
//        ThreadLocalUtil.set(Constants.USER_ID, userId);
//        ThreadLocalUtil.set(Constants.USER_KEY, userKey);
//        tokenService.extendToken(claims);
        return true;
    }

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(HttpConstants.AUTHENTICATION);
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, "");
        }
        return token;
    }


}

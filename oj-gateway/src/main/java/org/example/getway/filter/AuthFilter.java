package org.example.getway.filter;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.example.common.core.constants.CacheConstants;
import org.example.common.core.constants.HttpConstants;
import org.example.common.core.domain.LoginUser;
import org.example.common.core.domain.R;
import org.example.common.core.enums.ResultCode;
import org.example.common.core.enums.UserIdentity;
import org.example.common.core.utils.JwtUtils;
import org.example.getway.properties.IgnoreWhiteProperties;
import org.example.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 网关鉴权
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    // 排除过滤的 uri 白名单地址，在nacos自行添加
    @Autowired
    private IgnoreWhiteProperties ignoreWhite;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private RedisService redisService;

    //过滤不符合条件的接口地址
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath(); //请求的接口地址 登录接口是否需要进行身份认证？ 否
        // 跳过不需要验证的路径  接口白名单中的所有接口均不需要进行身份的认证
        if (matches(url, ignoreWhite.getWhites())) {
            //判断如果当前的接口在白名单中则不需要进行身份认证  ignoreWhite.getWhites(): 拿到nacos上配置的接口地址的白名单
            return chain.filter(exchange);
        }

        //执行到这说明接口不再白名单中，接着进行身份认证逻辑   通过token进行身份认证  首先要把token获取出来
        //从http请求头中获取token
        String token = getToken(request);
        if (StrUtil.isEmpty(token)) {
            return unauthorizedResponse(exchange, "令牌不能为空");
        }
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret); //获取令牌中信息  解析payload中信息  存储着用户唯一标识信息
            if (claims == null) {
                //springcloud gateway 基于webflux
                //我们之前写的全局异常处理使用了@RestControllerAdvice,是用于处理SpringMVC下的异常的，这里不适合
                return unauthorizedResponse(exchange, "令牌已过期或验证不正确！");
            }
        } catch (Exception e) {
            return unauthorizedResponse(exchange, "令牌已过期或验证不正确！");
        }

        //通过redis中存储的数据，来控制jwt的过期时间
        String userKey = JwtUtils.getUserKey(claims);  //获取jwt中的key
        boolean isLogin = redisService.hasKey(getTokenKey(userKey));
        if (!isLogin) {
            return unauthorizedResponse(exchange, "登录状态已过期");
        }
        String userId = JwtUtils.getUserId(claims);  //判断jwt中的信息是否完整
        if (StrUtil.isEmpty(userId)) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }

        //token 是正确的且没有过期
        //校验redis存储的信息和token中存储的key是不是一样的
        //判断当前请求是C端功能还是B端功能
        LoginUser user = redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);
        if (url.contains(HttpConstants.SYSTEM_URL_PREFIX) && !UserIdentity.ADMIN.getValue().equals(user.getIdentity())) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }
        if (url.contains(HttpConstants.FRIEND_URL_PREFIX) && !UserIdentity.ORDINARY.getValue().equals(user.getIdentity())) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }

        return chain.filter(exchange);
    }

    /**
     * 查找指定url是否匹配指定匹配规则链表中的任意一个字符串
     *
     * @param url         指定url
     * @param patternList 需要检查的匹配规则链表
     * @return 是否匹配
     */
    private boolean matches(String url, List<String> patternList) {
        if (StrUtil.isEmpty(url) || CollectionUtils.isEmpty(patternList)) {
            return false;
        }
        //接口地址如果和白名单中其中一个地址匹配就返回true。 如果便利完白名单中所有的地址都没有匹配的返回false
        for (String pattern : patternList) {
            if (isMatch(pattern, url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断url是否与规则匹配
     * @param pattern 匹配规则
     * @param url     需要匹配的url
     * @return 是否匹配
     */
    private boolean isMatch(String pattern, String url) {
        //pattern 中可以写一些特殊字符
        // ? 表示单个任意字符
        // * 表示一层路径内的任意字符串，不可跨层级
        // ** 表示任意层路径
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    /**
     * 获取缓存key
     */
    private String getTokenKey(String token) {
        return CacheConstants.LOGIN_TOKEN_KEY + token;
    }

    /**
     * 从请求头中获取请求token
     */
    private String getToken(ServerHttpRequest request) {
        //AUTHENTICATION-"Authorization"是HTTP请求中用于存放身份请求凭据的
        String token = request.getHeaders().getFirst(HttpConstants.AUTHENTICATION);
        // 如果前端设置了令牌前缀，则裁剪掉前缀，一般来说会包含"Bearer "
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return token;
    }

    //exchange -- 路径   msg -- 错误信息
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg) {
        log.error("[鉴权异常处理]请求路径:{}", exchange.getRequest().getPath());
        return webFluxResponseWriter(exchange.getResponse(), msg, ResultCode.FAILED_UNAUTHORIZED.getCode());
    }

    //拼装webflux模型响应
    private Mono<Void> webFluxResponseWriter(ServerHttpResponse response, String msg, int code) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        R<?> result = R.fail(code, msg);
        DataBuffer dataBuffer = response.bufferFactory().wrap(JSON.toJSONString(result).getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }

    @Override
    public int getOrder() {
        return -200;  //值越小 过滤器就越先被执行
    }

}

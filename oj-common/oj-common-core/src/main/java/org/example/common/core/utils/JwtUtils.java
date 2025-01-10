package org.example.common.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.HashMap;
import java.util.Map;

import static org.example.common.core.constants.JwtConstants.*;


//1.用户登录成功之后，调用createToken 生成令牌  并发送给客户端
//2. 后续的所有请求，再调用具体的接口之前，都要先通过token进行身份认证  --代码见gateway
//3、用户使用系统的过程中我们需要进行适时的延长jwt的过期时间
//因为每次登录的时候生成的jwt令牌是不一样的，如果多次登录，那么就会在缓存中占用大量资源，我们需要对此进行优化
public class JwtUtils {

    /**
     * 生成令牌
     * @param claims 数据
     * @param secret 密钥
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims, String secret) {
        String token = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secret).compact();
        return token;
    }

    /**
     * 从令牌中获取数据
     *
     * @param token  令牌
     * @param secret 密钥
     * @return 数据
     */
    public static Claims parseToken(String token, String secret) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }


    public static String getUserKey(Claims claims) {
        return toStr(claims.get(LOGIN_USER_KEY));
    }

    public static String getUserId(Claims claims) {
        return toStr(claims.get(LOGIN_USER_ID));
    }

    private static String toStr(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    public static void main(String[] args) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userpassword", 123456);
        //secret  保密 随机 不能硬编码  定期更换
        System.out.println(createToken(claims, "zxcvbnmasdfghjuiyreqtuiwq"));
        //String token = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEyMzQ1Njc4OX0.rgVT9HXxyzwEPq4c_2gznBlrJ0NIC1wkJZzYml6dddZxVFP0ELAMQRT-o8LcrEIFESHKEuMIfUjhBBvq12ucqw";

        //seret是密钥,这里随便打几个
        //Claims claim = parseToken(token, "zxcvbnmasdfghjuiyreqtuiwq");
        //System.out.println(claim);

    }
}

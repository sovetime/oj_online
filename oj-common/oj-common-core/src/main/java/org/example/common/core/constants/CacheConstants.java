package org.example.common.core.constants;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-31
 * Time: 14:13
 */
public class CacheConstants {
    /**
     * 缓存有效期，默认2880（分钟）,2天
     */
    public final static long EXPIRATION = 2880;

    /**
     * ⽤⼾⾝份认证缓存前缀
     */
    public final static String LOGIN_TOKEN_KEY = "logintoken:";

    /**
     * 在剩余3分钟以内的时候再次登录，更新缓存中jwt有效时间
     */
    public static final long REFRESH_TIME = 3;


}

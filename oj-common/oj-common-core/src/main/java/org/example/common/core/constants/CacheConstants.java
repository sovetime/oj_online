package org.example.common.core.constants;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-31
 * Time: 14:13
 */
public class CacheConstants {

    //缓存有效期，默认2880（分钟）,2天
    public final static long EXPIRATION = 2880;

    //用户⾝份认证缓存前缀
    public final static String LOGIN_TOKEN_KEY = "logintoken:";

    //在剩余30分钟以内的时候再次登录，更新缓存中jwt有效时间
    public static final long REFRESH_TIME = 30;

    public final static String PHONE_CODE_KEY = "p:c:";

    public final static String CODE_TIME_KEY = "c:t:";

    // 未完赛竞赛列表
    public final static String EXAM_UNFINISHED_LIST = "e:t:l";

    // 历史竞赛列表
    public final static String EXAM_HISTORY_LIST = "e:h:l";

    //竞赛详情信息
    public final static String EXAM_DETAIL = "e:d:";

    //用户竞赛列表
    public final static String USER_EXAM_LIST = "u:e:l:";

    //用户详情信息
    public final static String USER_DETAIL = "u:d:";

    public final static long USER_EXP = 10;

    public static final String USER_UPLOAD_TIMES_KEY = "u:u:t";

    public static final String QUESTION_LIST = "q:l";

    public static final String QUESTION_HOST_LIST = "q:h:l";

    public static final String EXAM_QUESTION_LIST = "e:q:l:";

    public static final String USER_MESSAGE_LIST = "u:m:l:";

    public static final String MESSAGE_DETAIL = "m:d:";

    public static final String EXAM_RANK_LIST = "e:r:l:";

    public static final long DEFAULT_START = 0;

    public static final long DEFAULT_END = -1;
}

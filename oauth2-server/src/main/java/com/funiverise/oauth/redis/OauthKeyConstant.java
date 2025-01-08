package com.funiverise.oauth.redis;

/**
 * @author jun
 * @version [1.0.0, 2023/06/26]
 * @description: 定义一些全局公用的key
 */
public interface OauthKeyConstant {



    String LOGIN_USER_PREFIX_ID = "LOGIN_USER_PREFIX:ID:";

    String LOGIN_USER_PREFIX_TOKEN = "LOGIN_USER_PREFIX:TOKEN:";

    String LOGIN_FAIL_USER = "LOGIN_FAIL_USER:";

    String ACCESS_VERIFY_RANDOM_KEY = "ACCESS_VERIFY_RANDOM_KEY:";

    String ONLINE_MEMBERS = "ONLINE_MEMBERS";

    String LOGIN_IP_2_USER = "LOGIN_IP_2_USER:";

    String OAUTH_REDIS_PREFIX = "OAUTH";

    String LOGIN_FAIL_IP = "LOGIN_FAIL_IP";

}

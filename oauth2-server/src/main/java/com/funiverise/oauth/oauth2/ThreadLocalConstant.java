package com.funiverise.oauth.oauth2;

import java.util.Map;

/**
 * @author jun
 * @version [1.0.0, 2023/07/19]
 * @description:
 */
public interface ThreadLocalConstant {

    ThreadLocal<Map<String, Object>> CURRENT_OAUTH_REQUEST = new InheritableThreadLocal<>();
}

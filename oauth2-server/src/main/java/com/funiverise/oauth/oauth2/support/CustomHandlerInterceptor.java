package com.funiverise.oauth.oauth2.support;

import com.guideir.common.util.IPUtil;
import com.guideir.oauth.oauth2.ThreadLocalConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jun
 * @version [1.0.0, 2023/07/18]
 * @description:
 */
@Slf4j
public class CustomHandlerInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String servletPath = request.getServletPath();
        String grantType = request.getParameter("grant_type");
        if (("/oauth/token".equals(servletPath) && "password".equals(grantType)) || "/oauth/third_party_login".equals(servletPath)) {
            Map<String, Object> currentLoginMap = ThreadLocalConstant.CURRENT_OAUTH_REQUEST.get();
            if (null == currentLoginMap) {
                Map<String, Object> map = new HashMap<>();
                ThreadLocalConstant.CURRENT_OAUTH_REQUEST.set(map);
                currentLoginMap = map;
            }
            String remoteAddr = IPUtil.getClientIp(request);
            log.info("当前用户的客户端ip ：{} ", remoteAddr);
            //本次登录的ip
            currentLoginMap.put("current_login_ip", remoteAddr);
            currentLoginMap.put("current_login_browser", request.getHeader("User-Agent"));
            currentLoginMap.put("current_login_normal", true);

        }
        return true;
    }
}

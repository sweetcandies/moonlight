package com.funiverise.oauth.oauth2.support;

import cn.hutool.json.JSONUtil;
import com.guideir.common.redis.GDRedisTemplateUtil;
import com.guideir.oauth.oauth2.ThreadLocalConstant;
import com.guideir.oauth.oauth2.support.service.Oauth2UserDetailService;
import com.guideir.oauth.oauth2.support.service.TokenEndpointService;
import com.guideir.oauth.redis.OauthKeyConstant;
import com.guideir.system.common.dto.GdUserDTO;
import com.guideir.system.common.dto.SecurityConfigDTO;
import com.guideir.system.feign.SystemFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author jun
 * @version [1.0.0, 2023/07/18]
 * @description: 用户登录成功后，判断用户是否被挤掉，是否发送通知
 */
@Slf4j
@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private GDRedisTemplateUtil redisTemplateUtil;
    @Autowired
    private TokenEndpointService tokenEndpointService;
    @Autowired
    private SystemFeignClient systemFeignClient;
    @Autowired
    private Oauth2UserDetailService oauth2UserDetailService;
    @Resource
    IpFilterStrategy ipFilterStrategy;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent authenticationSuccessEvent) {
        Map<String, Object> currentLoginMap = ThreadLocalConstant.CURRENT_OAUTH_REQUEST.get();
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authenticationSuccessEvent.getSource();
        Object principal = usernamePasswordAuthenticationToken.getPrincipal();
        if (principal instanceof User) {//第一次进来currentLoginMap为空，获取当前client登录记录
            //主动释放内存，不然会内存泄露
            ThreadLocalConstant.CURRENT_OAUTH_REQUEST.remove();
            Map<String, Object> map = new HashMap<>();

            User client = (User) principal;
            //当前登录的client_id
            String clientId = client.getUsername();
            map.put("client_id", clientId);
            ThreadLocalConstant.CURRENT_OAUTH_REQUEST.set(map);
            return;
        }
        //第二次进来currentLoginMap不为空，获取当前user登录记录
        String currentLoginIp = (String) currentLoginMap.get("current_login_ip");
        String currentLoginBrowser = (String) currentLoginMap.get("current_login_browser");

        Oauth2UserDetails oauth2UserDetails = (Oauth2UserDetails) principal;
        //本次登录的用户
        GdUserDTO currentLoginUser = oauth2UserDetails.getUser();
        // 登录成功后清空登录失败记录
        SecurityConfigDTO securityConfig = oauth2UserDetailService.getSecurityConfig();
        if (null != securityConfig) {
            redisTemplateUtil.set(OauthKeyConstant.LOGIN_FAIL_USER + currentLoginUser.getUsername(), securityConfig.getAccountLockTryCount());
        }
        String currentLoginUserId = currentLoginUser.getUserId().toString();
        try {
            //拿到上次请求信息
            String userJson = redisTemplateUtil.get(OauthKeyConstant.LOGIN_USER_PREFIX_ID + currentLoginUserId);
            if (StringUtils.isNotBlank(userJson)) {
                GdUserDTO user = JSONUtil.toBean(userJson, GdUserDTO.class);
                // 如果不同ip，但相同浏览器，则视为异常登录，退出之前的用户登录信息；
//                if (!user.getLastLoginIp().equals(currentLoginIp)
//                        && user.getLastLoginBrowser().equals(currentLoginBrowser)) {
                // 不再判断是否不同ip，只要调用了重新登录就会产生新的token
                    currentLoginMap.put("current_login_normal", false);
                    tokenEndpointService.clearToken(user.getToken());
//                }

            }
            currentLoginUser.setLastLoginIp(currentLoginIp);
            currentLoginUser.setLastLoginBrowser(currentLoginBrowser);
            systemFeignClient.updateUserForLogin(currentLoginUser);
            CompletableFuture.runAsync(() -> ipFilterStrategy.loginSuccessProcess(currentLoginIp));
        } catch (Exception e){
            log.info(e.getMessage());
        }

    }

}

package com.funiverise.oauth.oauth2.support;

import com.guideir.common.base.GDRetCode;
import com.guideir.common.exception.GDBizException;
import com.guideir.common.redis.GDRedisTemplateUtil;
import com.guideir.oauth.oauth2.ThreadLocalConstant;
import com.guideir.oauth.oauth2.support.service.Oauth2UserDetailService;
import com.guideir.oauth.redis.OauthKeyConstant;
import com.guideir.system.common.dto.SecurityConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by lihd on 2023/08/29 17:21
 **/
@Component
@Slf4j
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    @Resource
    private GDRedisTemplateUtil redisTemplateUtil;
    @Resource
    private Oauth2UserDetailService oauth2UserDetailService;
    @Resource
    private IpFilterStrategy ipFilterStrategy;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent authenticationFailureEvent) {
        //主动释放内存，不然会内存泄露
        try {
            SecurityConfigDTO securityConfig = oauth2UserDetailService.getSecurityConfig();
            if (null == securityConfig) {
                log.error("安全配置为空");
                return;
            }
            if (Boolean.FALSE.equals(securityConfig.getAccountLockEnable())) {
                return;
            }
            Integer ipFailCount = ipFilterStrategy.loginFailProcess();
            if (authenticationFailureEvent.getSource() instanceof Authentication) {
                Authentication authentication = (Authentication) authenticationFailureEvent.getSource();
                String key = OauthKeyConstant.LOGIN_FAIL_USER+ authentication.getName();
                Integer leftTryTimes = redisTemplateUtil.get(key);
                if (null == leftTryTimes) {
                    leftTryTimes = securityConfig.getAccountLockTryCount() - 1;
                } else if (leftTryTimes > 0) {
                    leftTryTimes--;
                } else {
                    throw new GDBizException(GDRetCode.USER_LOCKED, "账户或密码不正确，尝试次数过多，请"+ redisTemplateUtil.getKeyValidTime(key) / 1000 + "秒后重试！");
                }
                leftTryTimes = Math.max(Math.min(leftTryTimes, securityConfig.getAccountLockTryCount() - ipFailCount), 0);
                if (leftTryTimes < 1) {
                    redisTemplateUtil.set(key, 0, securityConfig.getAccountLockTime(), TimeUnit.SECONDS);
                    throw new GDBizException(GDRetCode.USER_LOCKED, "账户或密码不正确，尝试次数过多，请"+ securityConfig.getAccountLockTime() + "秒后重试！");
                }
                redisTemplateUtil.set(OauthKeyConstant.LOGIN_FAIL_USER + authentication.getName(), leftTryTimes, securityConfig.getAccountLockTime(), TimeUnit.SECONDS);
                throw new GDBizException(GDRetCode.PASSWORD_ERROR, "账户或密码不正确，还有" + leftTryTimes + "次尝试机会!");
            }
        }finally {
            ThreadLocalConstant.CURRENT_OAUTH_REQUEST.remove();
        }
    }
}

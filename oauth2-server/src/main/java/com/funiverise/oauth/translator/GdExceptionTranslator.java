package com.funiverise.oauth.translator;


import com.guideir.common.base.GDCommonResult;
import com.guideir.common.exception.GDBizException;
import com.guideir.common.redis.GDRedisTemplateUtil;
import com.guideir.oauth.oauth2.ThreadLocalConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.BadClientCredentialsException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.security.oauth2.common.exceptions.UnsupportedGrantTypeException;
import org.springframework.security.oauth2.common.exceptions.UnsupportedResponseTypeException;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 异常翻译
 *
 * @author MrBird
 */
@Slf4j
@Component
@SuppressWarnings("all")
public class GdExceptionTranslator implements WebResponseExceptionTranslator {

    @Resource
    private GDRedisTemplateUtil redisTemplateUtil;

    @Override
    public ResponseEntity<?> translate(Exception e) {
        //主动释放内存，不然会内存泄露
        ThreadLocalConstant.CURRENT_OAUTH_REQUEST.remove();
        ResponseEntity.BodyBuilder status = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        String message = "认证失败";
        log.error("认证失败：{}", e.getMessage(),e);
        if (e instanceof UnsupportedGrantTypeException) {
            message = "不支持该认证类型";
            return status.body(GDCommonResult.error(message));
        }
        if (e instanceof InvalidTokenException) {
            //message = "刷新令牌已过期，请重新登录";
            message = "无效token";
            return status.body(GDCommonResult.error(message));
        }
        if (e instanceof InvalidScopeException) {
            message = "不是有效的scope值";
            return status.body(GDCommonResult.error(message));
        }
        if (e instanceof RedirectMismatchException) {
            message = "redirect_uri值不正确";
            return status.body(GDCommonResult.error(message));
        }
        if (e instanceof BadClientCredentialsException) {
            message = "client值不合法";
            return status.body(GDCommonResult.error(message));
        }
        if (e instanceof UnsupportedResponseTypeException) {
            String code = StringUtils.substringBetween(e.getMessage(), "[", "]");
            message = code + "不是合法的response_type值";
            return status.body(GDCommonResult.error(message));
        }


        if (e instanceof InvalidGrantException) {
            if (StringUtils.containsIgnoreCase(e.getMessage(), "Invalid refresh token")) {
                message = "refresh token无效";
                return status.body(GDCommonResult.error(message));
            }
            if (StringUtils.containsIgnoreCase(e.getMessage(), "Invalid authorization code")) {
                String code = StringUtils.substringAfterLast(e.getMessage(), ": ");
                message = "授权码" + code + "不合法";
                return status.body(GDCommonResult.error(message));
            }
            if (StringUtils.containsIgnoreCase(e.getMessage(), "locked")) {
                message = "用户已被锁定，请联系管理员";
                return status.body(GDCommonResult.error(message));
            }
            if (StringUtils.containsIgnoreCase(e.getMessage(), "disabled")) {
                message = "登录失败，请联系管理员！";
                return status.body(GDCommonResult.error(message));
            }
            if (StringUtils.containsIgnoreCase(e.getMessage(), "expired")) {
                message = "账户已过期，请联系管理员！";
                return status.body(GDCommonResult.error(message));
            }
            message = "账户或密码不正确，请重新尝试！";
            return status.body(GDCommonResult.error(message));
        }
        if (e instanceof GDBizException) {
            message = e.getMessage();
        }
        if (e.getCause() instanceof GDBizException) {
            message = e.getMessage();
        }
        return status.body(GDCommonResult.error(message));
    }
}

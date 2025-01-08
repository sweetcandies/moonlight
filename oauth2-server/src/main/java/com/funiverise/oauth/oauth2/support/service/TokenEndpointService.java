package com.funiverise.oauth.oauth2.support.service;

import cn.hutool.json.JSONUtil;
import com.guideir.common.base.GDCommonResult;
import com.guideir.common.base.GDRetCode;
import com.guideir.common.redis.GDRedisTemplateUtil;
import com.guideir.oauth.redis.OauthKeyConstant;
import com.guideir.system.common.dto.GdUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @author jun
 * @version [1.0.0, 2023/07/07]
 * @description:
 */
@Slf4j
@Service
@Transactional
public class TokenEndpointService {

    @Autowired
    private GDRedisTemplateUtil redisTemplateUtil;

    @Autowired
    private ResourceServerTokenServices tokenServices;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private TokenStore tokenStore;
    public GdUserDTO getCurrentUser() {
        String token = request.getHeader("Authorization").split(" ")[1];
        GdUserDTO gdUser = JSONUtil.toBean(redisTemplateUtil.get(OauthKeyConstant.LOGIN_USER_PREFIX_TOKEN + token).toString(), GdUserDTO.class);
        if(gdUser == null){
            OAuth2Authentication oAuth2Authentication = tokenServices.loadAuthentication(token);
            gdUser = JSONUtil.toBean(JSONUtil.toJsonStr(oAuth2Authentication.getPrincipal()), GdUserDTO.class);
            redisTemplateUtil.set(OauthKeyConstant.LOGIN_USER_PREFIX_TOKEN + token, gdUser, 30L, TimeUnit.MINUTES);
        }
        return gdUser;
    }

    public Object clearToken(String token) {
        if (StringUtils.isBlank(token)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GDCommonResult.error(GDRetCode.BAD_REQUEST));
        }
        OAuth2AccessToken oauth2AccessToken = tokenStore.readAccessToken(token);
        if (oauth2AccessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GDCommonResult.error(GDRetCode.UNAUTHORIZED));
        }
        OAuth2RefreshToken refreshToken = oauth2AccessToken.getRefreshToken();
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GDCommonResult.error(GDRetCode.UNAUTHORIZED));
        }
        tokenStore.removeAccessToken(oauth2AccessToken);
        tokenStore.removeRefreshToken(refreshToken);

        String userJson = redisTemplateUtil.get(OauthKeyConstant.LOGIN_USER_PREFIX_TOKEN + oauth2AccessToken.getValue());

        if (StringUtils.isNotBlank(userJson)) {
            GdUserDTO user = JSONUtil.toBean(userJson, GdUserDTO.class);
            redisTemplateUtil.delete(OauthKeyConstant.LOGIN_USER_PREFIX_ID + user.getUserId());
        }
        redisTemplateUtil.delete(OauthKeyConstant.LOGIN_USER_PREFIX_TOKEN + oauth2AccessToken.getValue());
        //移除在状态
        redisTemplateUtil.removeSetValue(OauthKeyConstant.ONLINE_MEMBERS, oauth2AccessToken.getValue());


        return ResponseEntity.status(HttpStatus.OK).body(GDCommonResult.ok("token已清除"));
    }
}

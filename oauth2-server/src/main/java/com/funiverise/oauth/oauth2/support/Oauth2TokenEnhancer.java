package com.funiverise.oauth.oauth2.support;

import cn.hutool.core.util.RandomUtil;
import com.guideir.common.redis.GDRedisTemplateUtil;
import com.guideir.oauth.redis.OauthKeyConstant;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author gd11155-hlw
 * @desc: 类的描述:token自定义增强器(根据自己的业务需求添加非敏感字段)
 */
public class Oauth2TokenEnhancer implements TokenEnhancer {



    private GDRedisTemplateUtil redisTemplateUtil;


    public Oauth2TokenEnhancer setRedisSerializableUtils(GDRedisTemplateUtil util) {
        this.redisTemplateUtil = util;
        return this;
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {

        Oauth2UserDetails user = (Oauth2UserDetails) authentication.getPrincipal();

        final Map<String, Object> retMap = new HashMap<>();
        String randomNum = RandomUtil.randomString(32);
        retMap.put("randomNum", randomNum);
        redisTemplateUtil.set(OauthKeyConstant.ACCESS_VERIFY_RANDOM_KEY + randomNum, accessToken.getValue(),1L, TimeUnit.MINUTES);
        retMap.put("additionalInfo", user.getUser());

        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(retMap);

        return accessToken;
    }
}

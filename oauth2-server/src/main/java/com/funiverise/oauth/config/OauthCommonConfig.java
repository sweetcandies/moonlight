package com.funiverise.oauth.config;

import com.guideir.common.redis.GDRedisTemplateUtil;
import com.guideir.oauth.redis.OauthKeyConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @description: oauth服务通用配置
 * @author: hanyuefan
 * @version: [1.0.0, 2024/01/24]
 */
@Configuration
@ComponentScan("com.guideir.common.redis" )
public class OauthCommonConfig {


    @Bean("oauthRedisUtil")
    @Primary
    public GDRedisTemplateUtil oauthRedisUtil() {
        return new GDRedisTemplateUtil(OauthKeyConstant.OAUTH_REDIS_PREFIX);
    }
}

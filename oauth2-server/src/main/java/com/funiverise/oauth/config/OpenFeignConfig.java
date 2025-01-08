package com.funiverise.oauth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: hanyuefan
 * @version: [1.0.0, 2024/01/26]
 */
@Slf4j
@Configuration
@EnableFeignClients(basePackages = "com.guideir")
public class OpenFeignConfig {
}

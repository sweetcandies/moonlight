package com.funiverise.gateway.config.nacos;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: meiyang
 * @date: 2024年09月20日 13:42:55
 * @version:
 * @Description:
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnNacosDiscoveryEnabled
@LoadBalancerClients(defaultConfiguration = GDNacosLoadBalancerClientConfiguration.class)
public class GDLoadBalancerNacosAutoConfiguration {
}

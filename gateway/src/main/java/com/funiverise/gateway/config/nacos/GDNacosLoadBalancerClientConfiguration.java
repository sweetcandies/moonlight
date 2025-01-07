package com.funiverise.gateway.config.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancerClientConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * @Author: meiyang
 * @date: 2024年09月20日 13:38:01
 * @version:
 * @Description:
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@Import(NacosLoadBalancerClientConfiguration.class)
public class GDNacosLoadBalancerClientConfiguration {

    @Bean
    @ConditionalOnProperty(value = "gd.device.loadbalancer.enable", havingValue = "true", matchIfMissing = true)
    public ReactorLoadBalancer<ServiceInstance> nacosGDLoadBalancer(Environment environment,
                                                                    LoadBalancerClientFactory loadBalancerClientFactory,
                                                                    NacosDiscoveryProperties nacosDiscoveryProperties){
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new GDNacosCustomLoadBalancer(name, loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), nacosDiscoveryProperties);
    }
}

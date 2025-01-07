package com.funiverise.gateway.config.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Objects;

import static com.funiverise.common.constant.SystemConstant.SERVICE_INSTANCE_TAG;

/**
 * @Author: meiyang
 * @date: 2024年09月20日 08:47:07
 * @version:
 * @Description:
 */
@Configuration
@AutoConfigureBefore({NacosDiscoveryClientConfiguration.class})
public class GDNacosDiscoveryConfiguration {

    @Value("${gd.service.instance.tag:dev}")
    private String serviceInstanceTag;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.watch.enabled", matchIfMissing = true)
    public NacosWatch nacosWatch(NacosServiceManager nacosServiceManager, NacosDiscoveryProperties nacosDiscoveryProperties) {
        Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
        if (Objects.isNull(metadata)){
            metadata = Maps.newHashMap();
        }
        metadata.put(SERVICE_INSTANCE_TAG, serviceInstanceTag);
        return new NacosWatch(nacosServiceManager,nacosDiscoveryProperties);
    }
}

package com.funiverise.gateway.config.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.balancer.NacosBalancer;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.funiverise.common.constant.SystemConstant.SERVICE_INSTANCE_TAG;

/**
 * @Author: meiyang
 * @date: 2024年09月20日 09:55:33
 * @version:
 * @Description:  参考 NacosLoadBalancer  RoundRobinLoadBalancer 负载均衡机制
 * 目前项目中用的是RoundRobinLoadBalancer 所以nacos很多负载均配置并没有生效
 */
@Slf4j
public class GDNacosCustomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    private static final List<String> INTERCEPTOR_URL_PREFIX = Lists.newArrayList("/guide-mqtt/mqtt/file");

    public GDNacosCustomLoadBalancer(String serviceId, ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }

    /**
     * @author meiyang
     * @createTime 2024/9/20 13:04
     * @param
     * @return
     * @description 路由负载
     **/
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get().next().map(serviceInstances -> getInstanceResponse(serviceInstances, request));
    }

    /**
     * @author meiyang
     * @createTime 2024/9/20 13:08
     * @param
     * @return
     * @description 路由
     **/
    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> serviceInstances, Request request) {
        if (serviceInstances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }
        if (request instanceof DefaultRequest){
            DefaultRequest defaultRequest = (DefaultRequest) request;
            Object context = defaultRequest.getContext();
            if (context instanceof RequestDataContext){
                RequestDataContext requestDataContext = (RequestDataContext) context;
                HttpHeaders headers = requestDataContext.getClientRequest().getHeaders();
                URI uri = requestDataContext.getClientRequest().getUrl();
                String path = uri.getPath();
                List<String> serviceTags = Lists.newArrayList();
                if (StringUtils.isNotEmpty(path) && INTERCEPTOR_URL_PREFIX.stream().anyMatch(path::startsWith)){
                    String query = uri.getQuery();
                    if (StringUtils.isNotEmpty(query)){
                        String[] params = query.split("&");
                        for (String param : params) {
                            try {
                                String[] keyValue = param.split("=");
                                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                                String value = URLDecoder.decode(keyValue[1], "UTF-8");
                                if (Objects.equals(key, "dev")){
                                    serviceTags.add(value);
                                    break;
                                }
                            }catch (Exception e){
                                log.error("parse url: {} error", path, e);
                            }
                        }
                    }
                }else {
                    List<String> requestHeaders = headers.get(SERVICE_INSTANCE_TAG);
                    if (CollectionUtils.isNotEmpty(requestHeaders)){
                        serviceTags.addAll(requestHeaders);
                    }
                }
                if (CollectionUtils.isNotEmpty(serviceTags)){
                    String serviceTag = serviceTags.get(0);
                    List<ServiceInstance> filterInstances = serviceInstances.stream()
                            .filter(serviceInstance -> {
                                String cluster = serviceInstance.getMetadata().get(SERVICE_INSTANCE_TAG);
                                return StringUtils.equals(cluster, serviceTag);
                            }).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(filterInstances)){
                        ServiceInstance instance = NacosBalancer.getHostByRandomWeight3(filterInstances);
                        log.info("find service instance with tag: {}, host: {}, path: {}", serviceTag, uri.getHost(), uri.getPath());
                        return new DefaultResponse(instance);
                    }
                }
            }
        }
        return this.getClusterInstanceResponse(serviceInstances);
    }

    /**
     * @author meiyang
     * @createTime 2024/9/20 14:12
     * @param
     * @return
     * @description 本集群的的负载均衡
     **/
    private Response<ServiceInstance> getClusterInstanceResponse(List<ServiceInstance> serviceInstances) {
        if (serviceInstances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }
        try {
            String clusterName = this.nacosDiscoveryProperties.getClusterName();
            List<ServiceInstance> instancesToChoose = serviceInstances;
            if (StringUtils.isNotBlank(clusterName)) {
                List<ServiceInstance> sameClusterInstances = serviceInstances.stream()
                        .filter(serviceInstance -> {
                            String cluster = serviceInstance.getMetadata().get("nacos.cluster");
                            return StringUtils.equals(cluster, clusterName);
                        }).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(sameClusterInstances)) {
                    instancesToChoose = sameClusterInstances;
                }
            }
            else {
                log.warn("A cross-cluster call occurs，name = {}, clusterName = {}, instance = {}", serviceId, clusterName, serviceInstances);
            }
            ServiceInstance instance = NacosBalancer.getHostByRandomWeight3(instancesToChoose);
            return new DefaultResponse(instance);
        }
        catch (Exception e) {
            log.warn("NacosLoadBalancer error", e);
            return null;
        }
    }

    public static void main(String[] args) {
        String path = "/system-service/api/v1/systemConfig/getConfigByType/1";
        boolean match = INTERCEPTOR_URL_PREFIX.stream().anyMatch(i -> path.startsWith(i));
        System.out.println(match);
    }
}

package com.funiverise.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;

/**
 * @description:
 * @author: hanyuefan
 * @version: [1.0.0, 2023/10/17]
 */

@Component
public class AccessControlFilter implements GlobalFilter, Ordered {



    private static final Logger log = LoggerFactory.getLogger(AccessControlFilter.class);


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.defer(() ->
                        Mono.just(new SecurityContextImpl(new AnonymousAuthenticationToken("Key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))))
                ))
                .flatMap(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    if (authentication == null) {
                        return chain.filter(exchange);
                    }

                    Object principal = authentication.getPrincipal();
                    if (principal == null || "anonymousUser".equals(principal)) {
                        log.warn("当前为匿名用户, 请检查登录是否正常");
                        return chain.filter(exchange);
                    }
                    try {
                        OAuth2IntrospectionAuthenticatedPrincipal op = (OAuth2IntrospectionAuthenticatedPrincipal) principal;
                        JSONObject additionalInfo = new JSONObject();
                        if (op.getAttribute("additionalInfo") instanceof net.minidev.json.JSONObject) {
                            net.minidev.json.JSONObject info = op.getAttribute("additionalInfo");
                            if (null != info) {
                                additionalInfo = JSON.parseObject(info.toJSONString());
                            }
                        }
                        if (additionalInfo == null) {
                            return chain.filter(exchange);
                        }

                        Date now = new Date();
                        String startTimeStr = additionalInfo.getString("validStartTime");
                        String endTimeStr = additionalInfo.getString("validEndTime");
                        if (StringUtils.isBlank(startTimeStr) && StringUtils.isBlank(endTimeStr)) {
                            return chain.filter(exchange);
                        }

                    } catch (Exception e) {
                        log.error("用户登出失败 ", e);
                        return Mono.error(e);
                    }

                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }


}

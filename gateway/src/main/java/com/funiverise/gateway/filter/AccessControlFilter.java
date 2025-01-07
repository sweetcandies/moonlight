package com.funiverise.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guideir.common.base.GDCommonResult;
import com.guideir.common.base.GDRetCode;
import com.guideir.common.rocketmq.client.GDMqTemplate;
import com.guideir.common.rocketmq.constant.RocketMqSysConstant;
import com.guideir.common.rocketmq.domain.GDMqMessageDTO;
import com.guideir.common.rocketmq.domain.GDRocketMqEntityMessage;
import com.guideir.common.util.DateUtil;
import com.guideir.common.util.GDSnowFlakeGenerator;
import com.guideir.gateway.handler.GlobalExceptionHandler;
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

import javax.annotation.Resource;
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


    @Resource
    private GDMqTemplate mqTemplate;

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
                        Date validStartTime = DateUtil.string2Date(startTimeStr, DateUtil.yyyyMMddHHmmss);
                        Date validEndTime = DateUtil.string2Date(endTimeStr, DateUtil.yyyyMMddHHmmss);
                        String username = additionalInfo.getString("username");
                        // 用户是否处于有效期内
                        if (now.before(validStartTime) || now.after(validEndTime)) {
                            JSONObject message = new JSONObject();
                            message.put("usernames", Collections.singletonList(username));
                            message.put("reason", GDRetCode.USER_EXPIRED.getMessage());
                            GDRocketMqEntityMessage rocketMqEntityMessage = new GDRocketMqEntityMessage();
                            rocketMqEntityMessage.setData(message.toJSONString());
                            rocketMqEntityMessage.setKey(username + "_" + GDSnowFlakeGenerator.getNextId() );
                            rocketMqEntityMessage.setRetryCount(1);
                            rocketMqEntityMessage.setRpcInterface(false);
                            rocketMqEntityMessage.setSendTime(LocalDateTime.now());
                            GDMqMessageDTO mqMessageDTO = new GDMqMessageDTO();
                            mqMessageDTO.setTopic(RocketMqSysConstant.USER_LOGOUT_TOPIC);
                            mqMessageDTO.setPayload(rocketMqEntityMessage);
                            mqTemplate.asyncSendProducer(mqMessageDTO);
                            return GlobalExceptionHandler.resolveGenericException(exchange, GDCommonResult.error(GDRetCode.USER_EXPIRED));
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

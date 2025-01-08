package com.funiverise.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funiverise.common.base.CommonResult;
import com.funiverise.common.enums.RetCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import reactor.core.publisher.Mono;

/**
 * @description:
 * @author: hanyuefan
 * @version: [1.0.0, 2024/01/31]
 */
@Slf4j
public class AuthenticationFailureHandler implements ServerAuthenticationFailureHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException e) {
        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        log.error("认证失败：{}", e.getMessage(),e);

        CommonResult<Object> result = CommonResult.error(RetCode.NO_AUTH);
        byte[] value = new byte[0];
        try {
            value = objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException jsonProcessingException) {
            jsonProcessingException.printStackTrace();
        }
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON.toString());
        response.setStatusCode(HttpStatus.OK);
        DataBuffer wrap = response.bufferFactory().wrap(value);
        return response.writeWith(Mono.just(wrap));
    }
}
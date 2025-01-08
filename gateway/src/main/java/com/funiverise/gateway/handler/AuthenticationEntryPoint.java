package com.funiverise.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funiverise.common.base.CommonResult;
import com.funiverise.common.enums.RetCode;
import com.guideir.gateway.log.GdFailureLogHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @description:
 * @author: hanyuefan
 * @version: [1.0.0, 2024/01/31]
 */
@Slf4j
public class AuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public Mono<Void> commence(ServerWebExchange serverWebExchange, AuthenticationException e) {
        DataBuffer wrap = null;
        try {
            ServerHttpResponse response = serverWebExchange.getResponse();
            log.error("拒绝访问：{}", serverWebExchange.getRequest().getURI() + " : " + e.getMessage(), e);
            CommonResult<Object> result = CommonResult.error(RetCode.NO_AUTH);
            byte[] value = new byte[0];
            try {
                value = objectMapper.writeValueAsBytes(result);
            } catch (JsonProcessingException jsonProcessingException) {
                jsonProcessingException.printStackTrace();
            }
            response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON.toString());
            response.setStatusCode(HttpStatus.OK);
            wrap = response.bufferFactory().wrap(value);
            return response.writeWith(Mono.just(wrap));
        } finally {
            if (null != wrap) {
                DataBufferUtils.release(wrap);
            }
        }
    }

}

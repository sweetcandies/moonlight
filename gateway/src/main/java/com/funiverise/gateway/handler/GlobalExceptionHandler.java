package com.funiverise.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guideir.common.base.GDCommonResult;
import com.guideir.common.base.GDRetCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway 的全局异常处理器，将 Exception 翻译成 GDCommonResult + 对应的异常编号
 * @author jun
 */
@Component
@Order(-1) // 保证优先级高于默认的 Spring Cloud Gateway 的 ErrorWebExceptionHandler 实现
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 已经 commit，则直接返回异常
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 转换成 GDCommonResult
        GDCommonResult<?> result;
        if (ex instanceof ResponseStatusException) {
            result = responseStatusExceptionHandler(exchange, (ResponseStatusException) ex);
        } else {
            result = defaultExceptionHandler(exchange, ex);
        }

        // 返回给前端
        return writeJSON(exchange, result);
    }

    /**
     * 返回 JSON 字符串
     *
     * @param exchange 响应
     * @param object   对象，会序列化成 JSON 字符串
     */
    @SuppressWarnings("deprecation") // 必须使用 APPLICATION_JSON_UTF8_VALUE，否则会乱码
    public static Mono<Void> writeJSON(ServerWebExchange exchange, Object object) {
        // 设置 header
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
        // 设置 body
        return response.writeWith(Mono.fromSupplier(() -> {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                ObjectMapper mapper = new ObjectMapper();
                // 当 object 是 Throwable 类型时特别处理，防止出现循环引用的问题
                if (object instanceof Throwable) {
                    Throwable throwable = (Throwable) object;
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("exception", throwable.getClass().getName());
                    errorMap.put("message", throwable.getMessage());
                    return bufferFactory.wrap(mapper.writeValueAsBytes(errorMap));
                }
                return bufferFactory.wrap(mapper.writeValueAsBytes(object));
            } catch (Exception ex) {
                ServerHttpRequest request = exchange.getRequest();
                log.error("[writeJSON][uri({}/{}) 发生异常:]", request.getURI(), request.getMethod(), ex);
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }

    /**
     * 处理 Spring Cloud Gateway 默认抛出的 ResponseStatusException 异常
     */
    private GDCommonResult<?> responseStatusExceptionHandler(ServerWebExchange exchange,
                                                             ResponseStatusException ex) {
        // TODO 这里要精细化翻译，默认返回用户是看不懂的
        ServerHttpRequest request = exchange.getRequest();
        log.error("[responseStatusExceptionHandler][uri({}/{}) 发生异常]", request.getURI(), request.getMethod(), ex);
        return GDCommonResult.error(String.valueOf(ex.getStatus().value()), ex);
    }

    /**
     * 处理系统异常，兜底处理所有的一切
     */
    @ExceptionHandler(value = Exception.class)
    public GDCommonResult<?> defaultExceptionHandler(ServerWebExchange exchange,
            Throwable ex) {
        ServerHttpRequest request = exchange.getRequest();
        log.error("[defaultExceptionHandler][uri({}/{}) 发生异常:]", request.getURI(), request.getMethod(), ex);

        // 返回 ERROR GDCommonResult
        return GDCommonResult.error(GDRetCode.FAILED);
    }

    /**
     * 处理公共异常响应统一封装
     *
     * @param exchange
     * @param resultMap
     * @return
     */
    public static Mono<Void> resolveGenericException(ServerWebExchange exchange, GDCommonResult<?> result ) {

        return Mono.defer(() -> {
            byte[] bytes;
            try {
                bytes = new ObjectMapper().writeValueAsBytes(result);
            } catch (JsonProcessingException e) {
                log.error("网关响应异常：", e);
                throw new RuntimeException("信息序列化异常");
            } catch (Exception e) {
                log.error("网关响应异常：", e);
                throw new RuntimeException("写入响应异常");
            }
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON.toString());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Flux.just(buffer));
        });
    }
}

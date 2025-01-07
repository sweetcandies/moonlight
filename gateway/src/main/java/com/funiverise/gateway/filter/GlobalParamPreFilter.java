package com.funiverise.gateway.filter;


import com.funiverise.gateway.common.ClientBodyOutputMessage;
import com.funiverise.gateway.common.PayloadServerWebExchangeDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.funiverise.common.constant.SystemConstant.SERVICE_INSTANCE_TAG;
import static com.funiverise.gateway.constant.GatewayConstant.FORM_DATA_ATTR;
import static com.funiverise.gateway.constant.GatewayConstant.JSON_PARAMS;


/**
 * @description: 前置全局参数过滤器，在gateway中进行参数处理时，均从本过滤器中缓存的对象取，不再单独进行请求解析
 * @author: hanyuefan
 * @version: [1.0.0, 2024/01/03]
 */
@Slf4j
@Component
public class GlobalParamPreFilter implements GlobalFilter, Ordered {

    private final List<HttpMessageReader<?>> defaultMessageReaders = HandlerStrategies.withDefaults().messageReaders();

    @Value("${gd.service.instance.tag:dev}")
    private String serviceInstanceTag;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        customFillDeviceServiceTag(exchange.getRequest().getHeaders());
        MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
        PayloadServerWebExchangeDecorator decorator = new PayloadServerWebExchangeDecorator(exchange);

        if ((MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType))) {
            return decorator.getMultipartData().flatMap(formData -> {
                decorator.getAttributes().put(FORM_DATA_ATTR, formData);
                BodyInserters.MultipartInserter inserters = BodyInserters.fromMultipartData(formData);
                return recombineRequestBody(inserters, exchange, chain);
            });
        } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
            return decorator.getFormData().flatMap(formData -> {
                decorator.getAttributes().put(FORM_DATA_ATTR, formData);
                BodyInserters.FormInserter<String> inserters = BodyInserters.fromFormData(formData);
                return recombineRequestBody(inserters, exchange, chain);
            });
        } else if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            final ServerRequest serverRequest = ServerRequest
                    .create(exchange.mutate().request(exchange.getRequest()).build(), defaultMessageReaders);
            ParameterizedTypeReference<?> reference = new ParameterizedTypeReference<String>() {
            };
            Mono<String> mono = serverRequest.bodyToMono(reference).flatMap(body -> {
                decorator.getAttributes().put(JSON_PARAMS, body);
                return Mono.just(body.toString());
            });
            BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(mono, String.class);
            return recombineRequestBody(bodyInserter, exchange, chain);
        } else {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("code", 10240);
            resultMap.put("message", "不支持的请求格式");
            resultMap.put("data", null);
            // TODO 全局异常处理
            return chain.filter(exchange);
        }

    }

    private void customFillDeviceServiceTag(HttpHeaders headers) {
        HttpHeaders httpHeaders = HttpHeaders.readOnlyHttpHeaders(headers);
        httpHeaders.set(SERVICE_INSTANCE_TAG, serviceInstanceTag);
        HttpHeaders.readOnlyHttpHeaders(headers);
        log.info("fill service instance tag: {} success", serviceInstanceTag);
    }

    public static <T> Mono<Void> recombineRequestBody(BodyInserter<T, ? super ClientHttpRequest> bodyInserter,
                                                      ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        HttpHeaders headers = new HttpHeaders();
        headers.putAll(request.getHeaders());
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        ClientBodyOutputMessage outputMessage = new ClientBodyOutputMessage(exchange, headers);
        return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
            ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(request) {
                @NotNull
                @Override
                public HttpHeaders getHeaders() {
                    return headers;
                }

                @NotNull
                @Override
                public Flux<DataBuffer> getBody() {
                    return outputMessage.getBody();
                }
            };
            return chain.filter(exchange.mutate().request(decorator).build());
        }));
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

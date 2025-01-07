package com.funiverise.gateway.common;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @description:
 * @author: hanyuefan
 * @version: [1.0.0, 2023/12/22]
 */
public class ClientBodyOutputMessage implements ClientHttpRequest {

    private final DataBufferFactory bufferFactory;

    private final HttpHeaders httpHeaders;

    private boolean cached = false;

    private Flux<DataBuffer> body = null;

    private final ServerHttpRequest request;



    public ClientBodyOutputMessage(ServerWebExchange exchange, HttpHeaders httpHeaders) {
        this.bufferFactory = exchange.getResponse().bufferFactory();
        this.httpHeaders = httpHeaders;
        this.request = exchange.getRequest();
    }
    boolean isCached() {
        return this.cached;
    }

    public Flux<DataBuffer> getBody() {
        if (body == null) {
            return Flux
                    .error(new IllegalStateException("The body is not set. " + "Did handling complete with success?"));
        }
        return this.body;
    }
    @Override
    public DataBufferFactory bufferFactory() {
        return this.bufferFactory;
    }

    @Override
    public void beforeCommit(Supplier<? extends Mono<Void>> action) {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        this.body = Flux.from(body);
        this.cached = true;
        return Mono.empty();
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return writeWith(Flux.from(body).flatMap(p -> p));
    }

    @Override
    public Mono<Void> setComplete() {
        return writeWith(Flux.empty());
    }

    @Override
    public HttpMethod getMethod() {
        return request.getMethod();
    }

    @Override
    public URI getURI() {
        return request.getURI();
    }

    @Override
    public MultiValueMap<String, HttpCookie> getCookies() {
        return request.getCookies();
    }


    @Override
    public <T> T getNativeRequest() {
        return null;
    }


    @Override
    public HttpHeaders getHeaders() {
        return this.httpHeaders;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }
}

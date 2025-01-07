package com.funiverise.gateway.common;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Mono;

/**
 * @description:
 * @author: hanyuefan
 * @version: [1.0.0, 2023/12/15]
 */
public class PartnerServerHttpResponseDecorator extends ServerHttpResponseDecorator {

    PartnerServerHttpResponseDecorator(ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return super.writeAndFlushWith(body);
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return super.writeWith(body);
    }
}

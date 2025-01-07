package com.funiverise.gateway.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: hanyuefan
 * @version: [1.0.0, 2023/12/15]
 */
@Slf4j
public class PartnerServerHttpRequestDecorator extends ServerHttpRequestDecorator {

    private Flux<DataBuffer> body;

    PartnerServerHttpRequestDecorator(ServerHttpRequest delegate) {
        super(delegate);
        final String path = delegate.getURI().getPath();
        final String query = delegate.getURI().getQuery();
        final String method = Optional.of(delegate.getMethod()).orElse(HttpMethod.GET).name();
        final String headers = delegate.getHeaders().entrySet()
                .stream()
                .map(entry -> "            " + entry.getKey() + ": [" + String.join(";", entry.getValue()) + "]")
                .collect(Collectors.joining("\n"));
        if (log.isDebugEnabled()) {
            log.debug("\n" +
                    "HttpMethod : {}\n" +
                    "Uri        : {}\n" +
                    "Headers    : \n" +
                    "{}", method, path + (StringUtils.hasLength(query) ? "" : "?" + query), headers);
        }
        body = super.getBody();
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return body;
    }

}
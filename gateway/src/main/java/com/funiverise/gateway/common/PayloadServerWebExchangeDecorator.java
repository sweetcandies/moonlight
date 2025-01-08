package com.funiverise.gateway.common;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

/**
 * @description:
 * @author: hanyuefan
 * @version: [1.0.0, 2023/12/15]
 */
public class PayloadServerWebExchangeDecorator extends ServerWebExchangeDecorator {

    private final PartnerServerHttpRequestDecorator requestDecorator;

    private final PartnerServerHttpResponseDecorator responseDecorator;

    public PayloadServerWebExchangeDecorator(ServerWebExchange delegate) {
        super(delegate);
        requestDecorator = new PartnerServerHttpRequestDecorator(delegate.getRequest());
        responseDecorator = new PartnerServerHttpResponseDecorator(delegate.getResponse());
    }

    @Override
    public ServerHttpRequest getRequest() {
        return requestDecorator;
    }

    @Override
    public ServerHttpResponse getResponse() {
        return responseDecorator;
    }
}

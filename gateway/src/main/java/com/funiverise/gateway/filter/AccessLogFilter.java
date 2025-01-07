package com.funiverise.gateway.filter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guideir.common.rocketmq.client.GDMqTemplate;
import com.guideir.common.rocketmq.constant.RocketMqSysConstant;
import com.guideir.common.rocketmq.domain.GDMqMessageDTO;
import com.guideir.common.rocketmq.domain.GDRocketMqEntityMessage;
import com.guideir.common.util.GDSnowFlakeGenerator;
import com.guideir.common.util.IPUtil;
import com.guideir.gateway.log.GatewayLog;
import com.guideir.gateway.log.GatewayLogInfoFactory;
import com.guideir.gateway.log.GatewayLogType;
import com.guideir.gateway.log.LogProperties;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.guideir.common.base.SystemLogConstant.*;

/**
 * mingrt001
 * 20240709
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessLogFilter implements GlobalFilter, Ordered {

    private final LogProperties logProperties;
    private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();
    /**
     * default HttpMessageReader.
     */
    private static final List<HttpMessageReader<?>> MESSAGE_READERS = HandlerStrategies.withDefaults().messageReaders();
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final GDMqTemplate mqTemplate;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 判断是否打开相应是日志配置 ignore配置校验
        if (!logProperties.getEnabled() || hasIgnoredFlag(exchange, logProperties)) {
            return chain.filter(exchange);
        }
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.defer(() ->
                        Mono.just(new SecurityContextImpl(new AnonymousAuthenticationToken("Key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))))
                ))
                .flatMap(securityContext -> {
                    GatewayLog gatewayLog = parseGateway(exchange);
                    // 获得请求上下文
                    setCurrentUserInfo(securityContext, gatewayLog);
                    ServerHttpRequest request = exchange.getRequest();
                    MediaType mediaType = request.getHeaders().getContentType();
                    if (Objects.isNull(mediaType)) {
                        return writeNormalLog(exchange, chain, gatewayLog);
                    } else {
                        gatewayLog.setRequestContentType(mediaType.getType() + "/" + mediaType.getSubtype());
                        // 对不同的请求类型做相应的处理
                        if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                            return writeBodyLog(exchange, chain, gatewayLog);
                        } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType) || MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
                            return readFormData(exchange, chain, gatewayLog);
                        } else {
                            return writeBasicLog(exchange, chain, gatewayLog);
                        }
                    }
                });
    }

    public void buildMessageAndSendAsync(String message) {
        GDRocketMqEntityMessage rocketMqEntityMessage = new GDRocketMqEntityMessage();
        rocketMqEntityMessage.setData(message);
        rocketMqEntityMessage.setKey(UUID.randomUUID().toString().replace("-", ""));
        rocketMqEntityMessage.setRetryCount(3);
        GDMqMessageDTO mqMessageDTO = new GDMqMessageDTO();
        mqMessageDTO.setTopic(RocketMqSysConstant.OPERATOR_LOG_TOPIC);
        mqMessageDTO.setPayload(rocketMqEntityMessage);
        mqTemplate.asyncSendProducer(mqMessageDTO);
//        log.info("message-------{}", message);
    }

    /**
     * 校验白名单
     *
     * @param exchange
     * @param logProperties
     * @return
     */
    private Boolean hasIgnoredFlag(ServerWebExchange exchange, LogProperties logProperties) {
        List<String> ignoredPatterns = logProperties.getIgnoredPatterns();
        if (CollectionUtil.isEmpty(ignoredPatterns)) {
            return Boolean.FALSE;
        }
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        for (String pattern : ignoredPatterns) {
            if (antPathMatcher.match(pattern, uri.getPath())) {
                // 检查一下，是否在必须校验的路径里面
                if (!CollectionUtil.isEmpty(logProperties.getMustPatterns())) {
                    for (String mustPattern : logProperties.getMustPatterns()) {
                        if (antPathMatcher.match(mustPattern, uri.getPath())) {
                            // 必须校验的规则，那么就返回false
                            return Boolean.FALSE;
                        }
                    }
                }
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * 生成相应的报告并推送qq邮箱消息
     */
    private void report(GatewayLog gatewayLog) {
        buildMessageAndSendAsync(JSONUtil.toJsonStr(gatewayLog));
    }

    /**
     * 获得当前请求分发的路由
     *
     * @param exchange
     * @return
     */
    private Route getGatewayRoute(ServerWebExchange exchange) {
        return exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
    }

    private GatewayLog parseGateway(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        // 如果需要转换为HttpServletRequest
        String ip = IPUtil.getClientIp(request);
        String requestPath = request.getPath().pathWithinApplication().value();
        Route route = getGatewayRoute(exchange);
        GatewayLog gatewayLog = new GatewayLog();
        gatewayLog.setId(GDSnowFlakeGenerator.getNextId());
        gatewayLog.setSchema(request.getURI().getScheme());
        gatewayLog.setMethod(request.getMethodValue());
        gatewayLog.setRequestPath(requestPath);
        gatewayLog.setTargetServer(route == null ? "" : route.getId());
        gatewayLog.setIp(ip);
        gatewayLog.setRequestTime(new Date());
        return gatewayLog;
    }

    public void setCurrentUserInfo(SecurityContext securityContext, GatewayLog gatewayLog) {
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            return;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return;
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
            // 设置用户信息
            fillUserInfo(gatewayLog, additionalInfo);
        } catch (Exception e) {
            log.error("网关层获取用户信息失败 ", e);
        }
    }

    private Mono writeNormalLog(ServerWebExchange exchange, GatewayFilterChain chain, GatewayLog gatewayLog) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            int value = response.getStatusCode().value();
            gatewayLog.setCode(value);
            long executeTime = DateUtil.between(gatewayLog.getRequestTime(), new Date(), DateUnit.MS);
            gatewayLog.setExecuteTime(executeTime);
            ServerHttpRequest request = exchange.getRequest();
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            Map<String, String> paramsMap = new HashMap<>();
            if (CollectionUtil.isNotEmpty(queryParams)) {
                for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                    paramsMap.put(entry.getKey(), StrUtil.join(StrPool.COMMA, entry.getValue()));
                }
            }
            gatewayLog.setQueryParams(JSONUtil.toJsonStr(paramsMap));
            recordResponseLog(exchange, gatewayLog);
            GatewayLogInfoFactory.log(GatewayLogType.NORMAL_REQUEST, gatewayLog);
            // 推送相应的报告
            report(gatewayLog);
        }));
    }

    /**
     * 解决 request body 只能读取一次问题，
     * 参考: org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory
     *
     * @param exchange
     * @param chain
     * @param gatewayLog
     * @return
     */
    @SuppressWarnings("unchecked")
    private Mono writeBodyLog(ServerWebExchange exchange, GatewayFilterChain chain, GatewayLog gatewayLog) {
        ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
        Mono<String> modifiedBody = serverRequest.bodyToMono(String.class).flatMap(body -> {
            gatewayLog.setRequestBody(body);
            return Mono.just(body);
        });
        // 通过 BodyInserter 插入 body(支持修改body), 避免 request body 只能获取一次
        BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
        return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {                    // 重新封装请求
            ServerHttpRequest decoratedRequest = requestDecorate(exchange, headers, outputMessage);                    // 记录响应日志
            ServerHttpResponseDecorator decoratedResponse = recordResponseLog(exchange, gatewayLog);                    // 记录普通的
            return chain.filter(exchange.mutate().request(decoratedRequest).response(decoratedResponse).build()).then(Mono.fromRunnable(() -> {                                // 打印日志
                GatewayLogInfoFactory.log(GatewayLogType.APPLICATION_JSON_REQUEST, gatewayLog);
                // 推送相应的报告
                report(gatewayLog);
            }));
        }));
    }

    /**
     * 读取form-data数据
     *
     * @param exchange
     * @param chain
     * @param accessLog
     * @return
     */
    private Mono<Void> readFormData(ServerWebExchange exchange, GatewayFilterChain chain, GatewayLog accessLog) {
        return DataBufferUtils.join(exchange.getRequest().getBody()).flatMap(dataBuffer -> {
            DataBufferUtils.retain(dataBuffer);
            final Flux<DataBuffer> cachedFlux = Flux.defer(() -> Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));
            final ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return cachedFlux;
                }

                @Override
                public MultiValueMap<String, String> getQueryParams() {
                    return UriComponentsBuilder.fromUri(exchange.getRequest().getURI()).build().getQueryParams();
                }
            };
            final HttpHeaders headers = exchange.getRequest().getHeaders();
            if (headers.getContentLength() == 0) {
                return chain.filter(exchange);
            }
            ResolvableType resolvableType;
            if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(headers.getContentType())) {
                resolvableType = ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, Part.class);
            } else {
                //解析 application/x-www-form-urlencoded
                resolvableType = ResolvableType.forClass(String.class);
            }
            return MESSAGE_READERS.stream().filter(reader -> reader.canRead(resolvableType, mutatedRequest.getHeaders().getContentType())).findFirst().orElseThrow(() -> new IllegalStateException("no suitable HttpMessageReader.")).readMono(resolvableType, mutatedRequest, Collections.emptyMap()).flatMap(resolvedBody -> {
                if (resolvedBody instanceof MultiValueMap) {
                    LinkedMultiValueMap map = (LinkedMultiValueMap) resolvedBody;
                    if (CollectionUtil.isNotEmpty(map)) {
                        StringBuilder builder = new StringBuilder();
                        final Part bodyPartInfo = (Part) ((MultiValueMap) resolvedBody).getFirst("body");
                        if (bodyPartInfo instanceof FormFieldPart) {
                            String body = ((FormFieldPart) bodyPartInfo).value();
                            builder.append("body=").append(body);
                        }
                        accessLog.setRequestBody(builder.toString());
                    }
                } else {
                    accessLog.setRequestBody((String) resolvedBody);
                }
                //获取响应体
                ServerHttpResponseDecorator decoratedResponse = recordResponseLog(exchange, accessLog);
                return chain.filter(exchange.mutate().request(mutatedRequest).response(decoratedResponse).build()).then(Mono.fromRunnable(() -> {                                    // 打印日志
                    // 打印响应的日志
                    GatewayLogInfoFactory.log(GatewayLogType.FORM_DATA_REQUEST, accessLog);
                    // 推送相应的报告
                    report(accessLog);
                }));
            });
        });
    }

    private Mono<Void> writeBasicLog(ServerWebExchange exchange, GatewayFilterChain chain, GatewayLog accessLog) {
        return DataBufferUtils.join(exchange.getRequest().getBody()).flatMap(dataBuffer -> {
            DataBufferUtils.retain(dataBuffer);
            final Flux<DataBuffer> cachedFlux = Flux.defer(() -> Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));
            final ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return cachedFlux;
                }

                @Override
                public MultiValueMap<String, String> getQueryParams() {
                    return UriComponentsBuilder.fromUri(exchange.getRequest().getURI()).build().getQueryParams();
                }
            };
            StringBuilder builder = new StringBuilder();
            MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
            if (CollectionUtil.isNotEmpty(queryParams)) {
                for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                    builder.append(entry.getKey()).append("=").append(entry.getValue()).append(StrPool.COMMA);
                }
            }
            accessLog.setRequestBody(builder.toString());            // 获取请求体
            ServerHttpResponseDecorator decoratedResponse = recordResponseLog(exchange, accessLog);
            return chain.filter(exchange.mutate().request(mutatedRequest).response(decoratedResponse).build()).then(Mono.fromRunnable(() -> {                        // 打印日志
                GatewayLogInfoFactory.log(GatewayLogType.BASIC_REQUEST, accessLog);
                // 推送相应的报告
                report(accessLog);
            }));
        });
    }

    /**
     * 请求装饰器，重新计算 headers
     *
     * @param exchange
     * @param headers
     * @param outputMessage
     * @return
     */
    private ServerHttpRequestDecorator requestDecorate(ServerWebExchange exchange, HttpHeaders headers, CachedBodyOutputMessage outputMessage) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(super.getHeaders());
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }
                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }

    /**
     * 记录响应日志
     * 通过 DataBufferFactory 解决响应体分段传输问题。
     */
    private ServerHttpResponseDecorator recordResponseLog(ServerWebExchange exchange, GatewayLog gatewayLog) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory bufferFactory = response.bufferFactory();
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    // 计算执行时间
                    long executeTime = DateUtil.between(gatewayLog.getRequestTime(), new Date(), DateUnit.MS);
                    gatewayLog.setExecuteTime(executeTime);
                    // 获取响应类型，如果是 json 就打印
                    String originalResponseContentType = exchange.getAttribute(ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);//
                    gatewayLog.setCode(this.getStatusCode().value());
                    //
                    if (Objects.equals(this.getStatusCode(), HttpStatus.OK)
                            && !StringUtil.isNullOrEmpty(originalResponseContentType)
                            && originalResponseContentType.contains("application/json")) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        return super.writeWith(fluxBody.buffer().handle((dataBuffers, sink) -> {
                            // 在处理大型数据时，考虑使用流式API而不是将全部数据加载到内存
                            // 此处为了简化示例，我们依然使用了字符串，但是在实际应用中要根据数据大小考虑合适的处理方式
                            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                dataBuffers.forEach(buffer -> {
                                    byte[] array = new byte[buffer.readableByteCount()];
                                    buffer.read(array);
                                    DataBufferUtils.release(buffer);
                                    try {
                                        baos.write(array);
                                    } catch (IOException e) {
                                        sink.error(new RuntimeException("Error writing data to output stream", e));
                                    }
                                });
                                byte[] content = baos.toByteArray();
                                // 处理 content，比如解析JSON
                                String responseResult = new String(content, StandardCharsets.UTF_8);
                                // 如果是登录请求，要从响应的数据里面解析出用户信息
                                judgeIsUserLoginRequest(responseResult, gatewayLog);
                                // 对请求结果的状态做判断
                                setRequestResponseStatus(responseResult, response.getStatusCode(), gatewayLog);
                                // 只取前1000个字符串
                                gatewayLog.setResponseBody(responseResult.length() > 4000 ? responseResult.substring(0, 4000) : responseResult);
                                sink.next(bufferFactory.wrap(content));
                            } catch (IOException e) {
                                sink.error(new RuntimeException("Error releasing data buffers", e));
                            }
                        }));
                    } else {
                    }
                }
                return super.writeWith(body);
            }
        };
    }

    private void setRequestResponseStatus(String responseResult, HttpStatus httpStatus, GatewayLog gatewayLog) {
        if (httpStatus.equals(HttpStatus.OK)) {
            // 登录的接口排除在外
            String requestPath = gatewayLog.getRequestPath();
            if (StringUtils.hasText(requestPath)) {
                if (!LOGIN_URL.equals(requestPath)) {
                    JSONObject jsonObject = JSON.parseObject(responseResult);
                    String code = jsonObject.getString("code");
                    if (StringUtils.hasText(code) && "0".equals(code)) {
                        gatewayLog.setCode(HttpStatus.OK.value());
                        // 更细的区分
                        if (OPERATION_DEVICE_ULR.equals(requestPath)) {
                            handlerOperationDeviceRes(gatewayLog, jsonObject);
                        }
                    } else {
                        gatewayLog.setCode(HttpStatus.BAD_REQUEST.value());
                    }
                } else {
                    // 登录
                    gatewayLog.setCode(HttpStatus.OK.value());
                }
            } else {
                gatewayLog.setCode(HttpStatus.BAD_REQUEST.value());
            }
        } else {
            gatewayLog.setCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    private void handlerOperationDeviceRes(GatewayLog gatewayLog, JSONObject jsonObject) {
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            Integer retCode = data.getInteger("retcode");
            if (retCode != null && retCode == 0) {
                gatewayLog.setCode(HttpStatus.OK.value());
                Integer cmdType = data.getIntValue("cmdtype");
                if (cmdType != null && (cmdType == 522 || cmdType == 523)) {
                    gatewayLog.setCode(HttpStatus.BAD_REQUEST.value());
                    JSONObject message = data.getJSONObject("message");
                    if (message != null) {
                        String image_content = message.getString("image_content");
                        if (StringUtils.hasText(image_content)) {
                            gatewayLog.setCode(HttpStatus.OK.value());
                        }
                    }
                    return;
                }
                return;
            }
        }
        gatewayLog.setCode(HttpStatus.BAD_REQUEST.value());
    }

    private void judgeIsUserLoginRequest(String responseResult, GatewayLog gatewayLog) {
        if (StringUtils.hasText(gatewayLog.getRequestPath()) && LOGIN_URL.equals(gatewayLog.getRequestPath())) {
            JSONObject jsonObject = JSON.parseObject(responseResult);
            JSONObject additionalInfo = jsonObject.getJSONObject("additionalInfo");
            fillUserInfo(gatewayLog, additionalInfo);
        }
    }

    private void fillUserInfo(GatewayLog gatewayLog, JSONObject additionalInfo) {
        if (additionalInfo == null) {
            return;
        }
        // 从用户的登录信息获取一些必要字段
        Long userId = additionalInfo.getLong("userId");
        String username = additionalInfo.getString("username");
        String orgId = additionalInfo.getString("orgId");
        String orgName = additionalInfo.getString("orgName");
        gatewayLog.setUserId(userId);
        gatewayLog.setUserName(username);
        gatewayLog.setOrgCode(orgId);
        gatewayLog.setOrgName(orgName);
    }
}
package com.funiverise.gateway.log;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.guideir.common.rocketmq.client.GDMqTemplate;
import com.guideir.common.rocketmq.constant.RocketMqSysConstant;
import com.guideir.common.rocketmq.domain.GDMqMessageDTO;
import com.guideir.common.rocketmq.domain.GDRocketMqEntityMessage;
import com.guideir.common.util.GDSnowFlakeGenerator;
import com.guideir.common.util.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author mingruting
 * @version [1.0.0, 2024/07/10]
 * @description:
 */
@Slf4j
public class GdFailureLogHandler {

    private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();

    private GdFailureLogHandler() {
    }

    public static void recordLog(ServerWebExchange exchange) {
        GatewayLog gatewayLog = parseGateway(exchange);
        ServerHttpRequest request = exchange.getRequest();
        MediaType mediaType = request.getHeaders().getContentType();
        if (Objects.isNull(mediaType)) {
            writeNormalLog(exchange, gatewayLog);
        } else {
            gatewayLog.setRequestContentType(mediaType.getType() + "/" + mediaType.getSubtype());
            // 对不同的请求类型做相应的处理
            if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                writeBodyLog(exchange, gatewayLog);
            } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType) || MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
                readFormData(exchange, gatewayLog);
            } else {
                writeBasicLog(exchange, gatewayLog);
            }
        }
        buildMessageAndSendAsync(gatewayLog);
    }

    public static void buildMessageAndSendAsync(GatewayLog gatewayLog) {
        GDRocketMqEntityMessage rocketMqEntityMessage = new GDRocketMqEntityMessage();
        rocketMqEntityMessage.setData(JSONUtil.toJsonStr(gatewayLog));
        rocketMqEntityMessage.setKey(UUID.randomUUID().toString().replace("-", ""));
        rocketMqEntityMessage.setRetryCount(3);
        GDMqMessageDTO mqMessageDTO = new GDMqMessageDTO();
        mqMessageDTO.setTopic(RocketMqSysConstant.OPERATOR_LOG_TOPIC);
        mqMessageDTO.setPayload(rocketMqEntityMessage);
        GDMqTemplate mqTemplate = SpringUtil.getBean(GDMqTemplate.class);
        mqTemplate.asyncSendProducer(mqMessageDTO);
    }

    private static void readFormData(ServerWebExchange exchange, GatewayLog gatewayLog) {
        gatewayLog.setRequestBody("");
        GatewayLogInfoFactory.log(GatewayLogType.FORM_DATA_REQUEST, gatewayLog);
    }

    private static void writeBasicLog(ServerWebExchange exchange, GatewayLog gatewayLog) {
        StringBuilder builder = new StringBuilder();
        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
        if (CollectionUtil.isNotEmpty(queryParams)) {
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                builder.append(entry.getKey()).append("=").append(entry.getValue()).append(StrPool.COMMA);
            }
        }
        gatewayLog.setRequestBody(builder.toString());
        GatewayLogInfoFactory.log(GatewayLogType.BASIC_REQUEST, gatewayLog);
    }

    private static void writeBodyLog(ServerWebExchange exchange, GatewayLog gatewayLog) {
        ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
        Mono<String> modifiedBody = serverRequest.bodyToMono(String.class).flatMap(body -> Mono.just(body));
        String body = modifiedBody.toString();
        gatewayLog.setRequestBody(body);
        GatewayLogInfoFactory.log(GatewayLogType.APPLICATION_JSON_REQUEST, gatewayLog);
    }

    private static void writeNormalLog(ServerWebExchange exchange, GatewayLog gatewayLog) {
        ServerHttpRequest request = exchange.getRequest();
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        Map<String, String> paramsMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(queryParams)) {
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                paramsMap.put(entry.getKey(), StrUtil.join(StrPool.COMMA, entry.getValue()));
            }
        }
        gatewayLog.setQueryParams(JSONUtil.toJsonStr(paramsMap));
        GatewayLogInfoFactory.log(GatewayLogType.NORMAL_REQUEST, gatewayLog);
    }

    private static GatewayLog parseGateway(ServerWebExchange exchange) {
        GatewayLog gatewayLog = new GatewayLog();
        ServerHttpRequest request = exchange.getRequest();
        // 如果需要转换为HttpServletRequest
        String ip = IPUtil.getClientIp(request);
        String requestPath = request.getPath().pathWithinApplication().value();
        Route route = getGatewayRoute(exchange);
        gatewayLog.setCode(HttpStatus.UNAUTHORIZED.value());
        gatewayLog.setExecuteTime(0L);
        gatewayLog.setId(GDSnowFlakeGenerator.getNextId());
        gatewayLog.setSchema(request.getURI().getScheme());
        gatewayLog.setMethod(request.getMethodValue());
        gatewayLog.setRequestPath(requestPath);
        List<String> paths = Arrays.stream(requestPath.split("/")).filter(item -> StringUtils.hasText(item)).collect(Collectors.toList());
        gatewayLog.setTargetServer(route == null ? paths.get(0) : route.getId());
        gatewayLog.setIp(ip);
        gatewayLog.setRequestTime(new Date());
        return gatewayLog;
    }

    private static Route getGatewayRoute(ServerWebExchange exchange) {
        return exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
    }
}

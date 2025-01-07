package com.funiverise.gateway.log;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * mingrt001
 * 20240709
 */
@Slf4j
public class GatewayLogInfoFactory {
    // 定义一个网关自己的单独日志文件（需要在logback文件中定义）

    public static void log(String type, GatewayLog gatewayLog) {
        switch (type) {
            case GatewayLogType.APPLICATION_JSON_REQUEST:
            case GatewayLogType.FORM_DATA_REQUEST:
            case GatewayLogType.BASIC_REQUEST:
                log.info("{}---->[{}] {} {},route: {},status: {},excute: {} mills,requestBody: {}"
                        , gatewayLog.getId()
                        , gatewayLog.getIp()
                        , gatewayLog.getMethod()
                        , gatewayLog.getRequestPath()
                        , gatewayLog.getTargetServer()
                        , gatewayLog.getCode()
                        , gatewayLog.getExecuteTime()
                        , StrUtil.replace(gatewayLog.getRequestBody(), StrPool.LF, "")
                );
                break;
            case GatewayLogType.NORMAL_REQUEST:
                log.info("{}---->[{}] {} {},route: {},status: {},excute: {} mills,queryParams: {}"
                        , gatewayLog.getId()
                        , gatewayLog.getIp()
                        , gatewayLog.getMethod()
                        , gatewayLog.getRequestPath()
                        , gatewayLog.getTargetServer()
                        , gatewayLog.getCode()
                        , gatewayLog.getExecuteTime()
                        , gatewayLog.getQueryParams()
                );
                break;
            default:
                break;
        }
    }
}
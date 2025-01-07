package com.funiverise.gateway.log;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * mingrt001
 * 20240709
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = LogProperties.PREFIX)
public class LogProperties {

    public static final String PREFIX = "gateway.log.access";
    /**
     * 是否开启日志打印,默认不开启
     */
    private Boolean enabled = false;
    /**
     * 忽略的pattern
     */
    private List<String> ignoredPatterns;
    /**
     * 必须拦截的pattern
     */
    private List<String> mustPatterns;

    private ApiAlarmConfiguration fail = new ApiAlarmConfiguration();
    private SlowApiAlarmConfiguration slow = new SlowApiAlarmConfiguration();

    /**
     * 慢API报警配置
     */
    @Data
    public static class SlowApiAlarmConfiguration {
        /**
         * 是否开启API慢日志打印
         */
        private boolean alarm = true;
        /**
         * 报警阈值 （单位：毫秒）
         */
        private long threshold = 500;
    }

    /**
     * API异常报警(根据http状态码判定）
     */
    @Data
    public static class ApiAlarmConfiguration {
        /**
         * 是否开启异常报警 默认关闭
         */
        private boolean alarm = false;
        /**
         * 排除状态码
         */
        private List<Integer> exclusion;
    }
}
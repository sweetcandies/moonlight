package com.funiverise.gateway.config;


import com.funiverise.gateway.handler.AccessDeniedHandler;
import com.funiverise.gateway.handler.AuthenticationEntryPoint;
import com.funiverise.gateway.handler.AuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.server.WebFilter;

/**
 * @description:
 * @author: hanyuefan
 * @version: [1.0.0, 2024/01/26]
 */
@Configuration
@EnableWebFluxSecurity
public class ResourceServerConfig {

    @Value("${spring.security.oauth2.resourceserver.opaque-token.introspection-uri:}")
    private String introspectionUri;

    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-secret:}")
    private String clientSecret;



    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf().disable().authorizeExchange()
                .pathMatchers("/oauth-service/oauth/**", "/gateway-service/actuator/**", "/doc.html", "/swagger-resources/**", "/swagger/**", "/system-service/api/v1/systemConfig/**", "/picAnalyseRetNotify", "/inspection-service/api/v1/monitor/task/addTask",
                        "/*/v3/api-docs", "/*.js", "/*.css", "/system-service/*.png", "/*.jpg", "/*.ico", "/webjars/**", "/*.zip", "/guide-mqtt/mqtt/file/**", "/inspection-service/inspect/report/zip/**", "/guide-third-device/third/image/**", "/guide-device/hot/report/zip/**").permitAll()
//                .pathMatchers("/oauth-service/oauth/**","/gateway-service/actuator/**", "/system-service/api/v1/systemConfig/**", "/picAnalyseRetNotify", "/inspection-service/api/v1/monitor/task/addTask",
//                        "/system-service/*.png", "/*.jpg","/*.ico", "/webjars/**", "/*.zip", "/guide-mqtt/mqtt/file/**", "/inspection-service/inspect/report/zip/**", "/guide-third-device/third/image/**", "/guide-device/hot/report/zip/**").permitAll()
                .anyExchange().authenticated()
                .and().oauth2ResourceServer().opaqueToken()
                .introspectionUri(this.introspectionUri)
                .introspectionClientCredentials(this.clientId, this.clientSecret)
                .and()
                .accessDeniedHandler(new AccessDeniedHandler())
                .authenticationEntryPoint(new AuthenticationEntryPoint());
        final SecurityWebFilterChain build = http.build();

        build
                .getWebFilters()
                .collectList()
                .subscribe(
                        webFilters -> {
                            for (WebFilter filter : webFilters) {
                                if (filter instanceof AuthenticationWebFilter) {
                                    AuthenticationWebFilter awf = (AuthenticationWebFilter) filter;
                                    awf.setAuthenticationFailureHandler(new AuthenticationFailureHandler());
                                }
                            }
                        });
        return build;
    }


}

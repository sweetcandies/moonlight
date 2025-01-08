package com.funiverise.oauth.config;


import com.funiverise.oauth.oauth2.support.Oauth2UserDetails;
import com.funiverise.oauth.oauth2.support.service.Oauth2UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;
import java.time.Duration;

/**
 * @desc: 类的描述:认证服务器配置
 */
@Configuration
public class Oauth2AuthorizationServerConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private Oauth2UserDetailService oauth2UserDetailService;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        // 使用数据库存储客户端信息
        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(dataSource);
        return repository;
    }

    @Bean
    public JdbcOAuth2AuthorizationService authorizationService() {
        // 使用数据库存储授权信息
        return new JdbcOAuth2AuthorizationService(dataSource, registeredClientRepository());
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            if (context.getPrincipal() instanceof Oauth2UserDetails) {
                Oauth2UserDetails userDetails = context.getPrincipal();
                context.getClaims().claim("username", userDetails.getUsername());
            }
        };
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.csrf().disable();
        return http.build();
    }

    @Bean
    public ProviderSettings providerSettings() {
        return ProviderSettings.builder().build();
    }

    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(1))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(false)
                .build();
    }

    @Bean
    public ClientSettings clientSettings() {
        return ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .requireProofKey(true)
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(JwkSourceUtils.jwkSource());
    }

    @Bean
    public TokenGenerator<OAuth2AccessToken> accessTokenGenerator(JwtEncoder jwtEncoder) {
        return new JwtAccessTokenGenerator(jwtEncoder);
    }
}


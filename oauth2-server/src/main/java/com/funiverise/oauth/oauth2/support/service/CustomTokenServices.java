package com.funiverise.oauth.oauth2.support.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.guideir.common.base.GDCommonResult;
import com.guideir.common.exception.GDBizException;
import com.guideir.common.redis.GDRedisTemplateUtil;
import com.guideir.oauth.oauth2.ThreadLocalConstant;
import com.guideir.oauth.oauth2.support.Oauth2UserDetails;
import com.guideir.oauth.redis.OauthKeyConstant;
import com.guideir.system.common.dto.GdUserDTO;
import com.guideir.system.feign.SystemFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author jun
 * @version [1.0.0, 2023/07/19]
 * @description: 自定义token处理逻辑，更灵活的支持业务相关自定义
 */
@Slf4j
public class CustomTokenServices implements AuthorizationServerTokenServices, ResourceServerTokenServices,
        ConsumerTokenServices, InitializingBean {

    private static final BytesKeyGenerator DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(20);

    private static final Charset US_ASCII = StandardCharsets.US_ASCII;

    private int refreshTokenValiditySeconds = 60 * 60 * 24 * 30; // default 30 days.

    private int accessTokenValiditySeconds = 60 * 60 * 12; // default 12 hours.

    private boolean supportRefreshToken = false;

    private boolean reuseRefreshToken = true;

    private TokenStore tokenStore;

    private ClientDetailsService clientDetailsService;

    private TokenEnhancer accessTokenEnhancer;

    private AuthenticationManager authenticationManager;
    @Autowired
    private SystemFeignClient systemFeignClient;
    @Autowired
    private ApplicationEventPublisher eventPublisher;


    /**
     * Initialize these token services. If no random generator is set, one will be created.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(tokenStore, "tokenStore must be set");
    }

    @Override
    @Transactional
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
        try {
            GDRedisTemplateUtil redisTemplateUtil = SpringUtil.getBean(GDRedisTemplateUtil.class);
            OAuth2AccessToken accessToken = getToken(authentication, redisTemplateUtil);

            Oauth2UserDetails oauth2UserDetails = (Oauth2UserDetails) authentication.getPrincipal();
            //本次登录的用户
            GdUserDTO currentLoginUser = oauth2UserDetails.getUser();
            currentLoginUser.setToken(accessToken.getValue());
            String currentLoginUserId = currentLoginUser.getUserId().toString();
            log.info("登录成功");
            currentLoginUser.setLastLoginTime(new Date());
            systemFeignClient.updateUserForLogin(currentLoginUser);
            //登录成功 清除当前用户错误的登录记录
            redisTemplateUtil.delete(OauthKeyConstant.LOGIN_FAIL_USER + currentLoginUser.getUsername());
            redisTemplateUtil.set(OauthKeyConstant.LOGIN_USER_PREFIX_TOKEN + accessToken.getValue(), JSONUtil.toJsonStr(currentLoginUser), Long.valueOf(accessToken.getExpiresIn()), TimeUnit.SECONDS);
            redisTemplateUtil.set(OauthKeyConstant.LOGIN_USER_PREFIX_ID + currentLoginUserId, JSONUtil.toJsonStr(currentLoginUser), Long.valueOf(accessToken.getExpiresIn()), TimeUnit.SECONDS);
            redisTemplateUtil.addSet(OauthKeyConstant.ONLINE_MEMBERS, Collections.singletonList(accessToken.getValue()));
            return accessToken;
        } finally {
            //主动释放内存，不然会内存泄露
            ThreadLocalConstant.CURRENT_OAUTH_REQUEST.remove();
        }
    }



    public OAuth2AccessToken getToken(OAuth2Authentication authentication, GDRedisTemplateUtil gdRedisTemplateUtil) {
        log.info("==================================================> 自定义token处理获取token");
        //拿到当前请求信息
        Map<String, Object> currentLoginMap = ThreadLocalConstant.CURRENT_OAUTH_REQUEST.get();
        //找到当前的token
        OAuth2AccessToken existingAccessToken = tokenStore.getAccessToken(authentication);

        OAuth2RefreshToken refreshToken = null;
        if (existingAccessToken != null) {
            // 每次登录需重新发放随机数
            Map<String, Object> additionalInformation = existingAccessToken.getAdditionalInformation();
            String randomNum = RandomUtil.randomString(32);
            additionalInformation.put("randomNum", randomNum);
            gdRedisTemplateUtil.set(OauthKeyConstant.ACCESS_VERIFY_RANDOM_KEY + randomNum, existingAccessToken.getValue(), 1L, TimeUnit.MINUTES);
            refreshToken = existingAccessToken.getRefreshToken();
            if (existingAccessToken.isExpired()) {//当前存在token但是已经超时，直接都删除
                gdRedisTemplateUtil.delete(OauthKeyConstant.LOGIN_USER_PREFIX_TOKEN + existingAccessToken.getValue());
                gdRedisTemplateUtil.removeSetValue(OauthKeyConstant.ONLINE_MEMBERS, existingAccessToken.getValue());
                removeExistToken(refreshToken, existingAccessToken);
            } else {//当前存在token且未超时，更新现有token
                //考虑同账号在不同机器登录的情景
                Object currentLoginNormal = currentLoginMap.get("current_login_normal");
                if (!(Boolean) currentLoginNormal) {//非正常登录
                    log.info("非正常登录,删除现有token，生成新的token");
                    removeExistToken(refreshToken, existingAccessToken);
                    gdRedisTemplateUtil.delete(OauthKeyConstant.LOGIN_USER_PREFIX_TOKEN + existingAccessToken.getValue());
//                    gdRedisTemplateUtil.delSetKey(OauthKeyConstant.ONLINE_MEMBERS, existingAccessToken.getValue());
                    return createNewToken(refreshToken, authentication);
                }
                // Re-store the access token in case the authentication has changed
                tokenStore.storeAccessToken(existingAccessToken, authentication);
                return existingAccessToken;
            }
        }

        //不存在token，直接创建新的token
        OAuth2AccessToken newToken = createNewToken(refreshToken, authentication);
        return newToken;
    }

    @Transactional
    public void removeExistToken(OAuth2RefreshToken refreshToken, OAuth2AccessToken existingAccessToken) {
        if (refreshToken != null) {
            tokenStore.removeRefreshToken(refreshToken);
        }
        tokenStore.removeAccessToken(existingAccessToken);
    }

    @Transactional
    public OAuth2AccessToken createNewToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {

        if (refreshToken == null) {
            refreshToken = createRefreshToken(authentication);
        }
        else if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
            ExpiringOAuth2RefreshToken expiring = (ExpiringOAuth2RefreshToken) refreshToken;
            if (System.currentTimeMillis() > expiring.getExpiration().getTime()) {
                refreshToken = createRefreshToken(authentication);
            }
        }

        OAuth2AccessToken accessToken = createAccessToken(authentication, refreshToken);
        tokenStore.storeAccessToken(accessToken, authentication);
        // In case it was modified
        refreshToken = accessToken.getRefreshToken();
        if (refreshToken != null) {
            tokenStore.storeRefreshToken(refreshToken, authentication);
        }
        return accessToken;
    }

    @Override
    @Transactional(noRollbackFor = {InvalidTokenException.class, InvalidGrantException.class})
    public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest)
            throws AuthenticationException {

        if (!supportRefreshToken) {
            throw new InvalidGrantException("Invalid refresh token");
        }

        OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(refreshTokenValue);
        if (refreshToken == null) {
            throw new InvalidGrantException("Invalid refresh token");
        }

        OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);
        if (this.authenticationManager != null && !authentication.isClientOnly()) {
            // The client has already been authenticated, but the user authentication might be old now, so give it a
            // chance to re-authenticate.
            Authentication userAuthentication = authentication.getUserAuthentication();
            PreAuthenticatedAuthenticationToken preAuthenticatedToken = new PreAuthenticatedAuthenticationToken(
                    userAuthentication,
                    "",
                    authentication.getAuthorities()
            );
            if (userAuthentication.getDetails() != null) {
                preAuthenticatedToken.setDetails(userAuthentication.getDetails());
            }
            Authentication user = authenticationManager.authenticate(preAuthenticatedToken);
            Object details = authentication.getDetails();
            authentication = new OAuth2Authentication(authentication.getOAuth2Request(), user);
            authentication.setDetails(details);
        }
        String clientId = authentication.getOAuth2Request().getClientId();
        if (clientId == null || !clientId.equals(tokenRequest.getClientId())) {
            throw new InvalidGrantException("Wrong client for this refresh token");
        }

        // clear out any access tokens already associated with the refresh
        // token.
        tokenStore.removeAccessTokenUsingRefreshToken(refreshToken);

        if (isExpired(refreshToken)) {
            tokenStore.removeRefreshToken(refreshToken);
            throw new InvalidTokenException("Invalid refresh token (expired)");
        }

        authentication = createRefreshedAuthentication(authentication, tokenRequest);

        if (!reuseRefreshToken) {
            tokenStore.removeRefreshToken(refreshToken);
            refreshToken = createRefreshToken(authentication);
        }

        OAuth2AccessToken accessToken = createAccessToken(authentication, refreshToken);
        tokenStore.storeAccessToken(accessToken, authentication);
        if (!reuseRefreshToken) {
            tokenStore.storeRefreshToken(accessToken.getRefreshToken(), authentication);
        }
        return accessToken;
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        return tokenStore.getAccessToken(authentication);
    }

    /**
     * Create a refreshed authentication.
     *
     * @param authentication The authentication.
     * @param request        The scope for the refreshed token.
     * @return The refreshed authentication.
     * @throws InvalidScopeException If the scope requested is invalid or wider than the original scope.
     */
    private OAuth2Authentication createRefreshedAuthentication(OAuth2Authentication authentication, TokenRequest request) {
        OAuth2Authentication narrowed = authentication;
        Set<String> scope = request.getScope();
        OAuth2Request clientAuth = authentication.getOAuth2Request().refresh(request);
        if (scope != null && !scope.isEmpty()) {
            Set<String> originalScope = clientAuth.getScope();
            if (originalScope == null || !originalScope.containsAll(scope)) {
                throw new InvalidScopeException("Unable to narrow the scope of the client authentication", originalScope);
            } else {
                clientAuth = clientAuth.narrowScope(scope);
            }
        }
        narrowed = new OAuth2Authentication(clientAuth, authentication.getUserAuthentication());
        return narrowed;
    }

    protected boolean isExpired(OAuth2RefreshToken refreshToken) {
        if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
            ExpiringOAuth2RefreshToken expiringToken = (ExpiringOAuth2RefreshToken) refreshToken;
            return expiringToken.getExpiration() == null
                    || System.currentTimeMillis() > expiringToken.getExpiration().getTime();
        }
        return false;
    }

    @Override
    public OAuth2AccessToken readAccessToken(String accessToken) {
        return tokenStore.readAccessToken(accessToken);
    }

    @Override
    public OAuth2Authentication loadAuthentication(String accessTokenValue) throws AuthenticationException,
            InvalidTokenException {
        OAuth2AccessToken accessToken = tokenStore.readAccessToken(accessTokenValue);
        if (accessToken == null) {
            throw new InvalidTokenException("Invalid access token");
        } else if (accessToken.isExpired()) {
            tokenStore.removeAccessToken(accessToken);
            throw new InvalidTokenException("Access token expired");
        }

        OAuth2Authentication result = tokenStore.readAuthentication(accessToken);
        if (result == null) {
            // in case of race condition
            throw new InvalidTokenException("Invalid access token");
        }
        if (clientDetailsService != null) {
            String clientId = result.getOAuth2Request().getClientId();
            try {
                clientDetailsService.loadClientByClientId(clientId);
            } catch (ClientRegistrationException e) {
                throw new InvalidTokenException("Client not valid", e);
            }
        }
        return result;
    }

    public String getClientId(String tokenValue) {
        OAuth2Authentication authentication = tokenStore.readAuthentication(tokenValue);
        if (authentication == null) {
            throw new InvalidTokenException("Invalid access token");
        }
        OAuth2Request clientAuth = authentication.getOAuth2Request();
        if (clientAuth == null) {
            throw new InvalidTokenException("Invalid access token (no client id)");
        }
        return clientAuth.getClientId();
    }

    @Override
    @Transactional
    public boolean revokeToken(String tokenValue) {
        OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
        if (accessToken == null) {
            return false;
        }
        if (accessToken.getRefreshToken() != null) {
            tokenStore.removeRefreshToken(accessToken.getRefreshToken());
        }
        tokenStore.removeAccessToken(accessToken);
        return true;
    }

    private OAuth2RefreshToken createRefreshToken(OAuth2Authentication authentication) {
        if (!isSupportRefreshToken(authentication.getOAuth2Request())) {
            return null;
        }
        int validitySeconds = getRefreshTokenValiditySeconds(authentication.getOAuth2Request());
        String tokenValue = new String(Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), US_ASCII);
        if (validitySeconds > 0) {
            return new DefaultExpiringOAuth2RefreshToken(tokenValue, new Date(System.currentTimeMillis()
                    + (validitySeconds * 1000L)));
        }
        return new DefaultOAuth2RefreshToken(tokenValue);
    }

    private OAuth2AccessToken createAccessToken(OAuth2Authentication authentication, OAuth2RefreshToken refreshToken) {
        String tokenValue = new String(Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), US_ASCII);
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(tokenValue);
        int validitySeconds = getAccessTokenValiditySeconds(authentication.getOAuth2Request());
        if (validitySeconds > 0) {
            token.setExpiration(new Date(System.currentTimeMillis() + (validitySeconds * 1000L)));
        }
        token.setRefreshToken(refreshToken);
        token.setScope(authentication.getOAuth2Request().getScope());

        return accessTokenEnhancer != null ? accessTokenEnhancer.enhance(token, authentication) : token;
    }

    /**
     * The access token validity period in seconds
     *
     * @param clientAuth the current authorization request
     * @return the access token validity period in seconds
     */
    protected int getAccessTokenValiditySeconds(OAuth2Request clientAuth) {
        if (clientDetailsService != null) {
            ClientDetails client = clientDetailsService.loadClientByClientId(clientAuth.getClientId());
            Integer validity = client.getAccessTokenValiditySeconds();
            if (validity != null) {
                return validity;
            }
        }
        return accessTokenValiditySeconds;
    }

    /**
     * The refresh token validity period in seconds
     *
     * @param clientAuth the current authorization request
     * @return the refresh token validity period in seconds
     */
    protected int getRefreshTokenValiditySeconds(OAuth2Request clientAuth) {
        if (clientDetailsService != null) {
            ClientDetails client = clientDetailsService.loadClientByClientId(clientAuth.getClientId());
            Integer validity = client.getRefreshTokenValiditySeconds();
            if (validity != null) {
                return validity;
            }
        }
        return refreshTokenValiditySeconds;
    }

    /**
     * Is a refresh token supported for this client or the global setting if
     * {@link #setClientDetailsService(ClientDetailsService) clientDetailsService} is not set.
     *
     * @param clientAuth the current authorization request
     * @return boolean to indicate if refresh token is supported
     */
    protected boolean isSupportRefreshToken(OAuth2Request clientAuth) {
        if (clientDetailsService != null) {
            ClientDetails client = clientDetailsService.loadClientByClientId(clientAuth.getClientId());
            return client.getAuthorizedGrantTypes().contains("refresh_token");
        }
        return this.supportRefreshToken;
    }

    /**
     * An access token enhancer that will be applied to a new token before it is saved in the token store.
     *
     * @param accessTokenEnhancer the access token enhancer to set
     */
    public void setTokenEnhancer(TokenEnhancer accessTokenEnhancer) {
        this.accessTokenEnhancer = accessTokenEnhancer;
    }

    /**
     * The validity (in seconds) of the refresh token. If less than or equal to zero then the tokens will be
     * non-expiring.
     *
     * @param refreshTokenValiditySeconds The validity (in seconds) of the refresh token.
     */
    public void setRefreshTokenValiditySeconds(int refreshTokenValiditySeconds) {
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    /**
     * The default validity (in seconds) of the access token. Zero or negative for non-expiring tokens. If a client
     * details service is set the validity period will be read from the client, defaulting to this value if not defined
     * by the client.
     *
     * @param accessTokenValiditySeconds The validity (in seconds) of the access token.
     */
    public void setAccessTokenValiditySeconds(int accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    /**
     * Whether to support the refresh token.
     *
     * @param supportRefreshToken Whether to support the refresh token.
     */
    public void setSupportRefreshToken(boolean supportRefreshToken) {
        this.supportRefreshToken = supportRefreshToken;
    }

    /**
     * Whether to reuse refresh tokens (until expired).
     *
     * @param reuseRefreshToken Whether to reuse refresh tokens (until expired).
     */
    public void setReuseRefreshToken(boolean reuseRefreshToken) {
        this.reuseRefreshToken = reuseRefreshToken;
    }

    /**
     * The persistence strategy for token storage.
     *
     * @param tokenStore the store for access and refresh tokens.
     */
    public void setTokenStore(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    /**
     * An authentication manager that will be used (if provided) to check the user authentication when a token is
     * refreshed.
     *
     * @param authenticationManager the authenticationManager to set
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * The client details service to use for looking up clients (if necessary). Optional if the access token expiry is
     * set globally via {@link #setAccessTokenValiditySeconds(int)}.
     *
     * @param clientDetailsService the client details service
     */
    public void setClientDetailsService(ClientDetailsService clientDetailsService) {
        this.clientDetailsService = clientDetailsService;
    }

    public OAuth2AccessToken processThirdLogin(String token) {
        try {
            GDCommonResult<GdUserDTO> result = systemFeignClient.loadUserByThirdToken(token);
            if (result == null || !result.success()) throw new GDBizException("认证获取用户信息失败");
            GdUserDTO user = result.getData();
            // 模拟认证成功
            Authentication authentication = createAuthentication(user);

            // 生成 OAuth2Authentication
            OAuth2Authentication oAuth2Authentication = createOAuth2Authentication(authentication);

            // 发布登录成功消息
            AuthenticationSuccessEvent successEvent = new AuthenticationSuccessEvent(authentication);
            eventPublisher.publishEvent(successEvent);
            // 使用 OAuth2 的 tokenServices 生成 access_token
            return createAccessToken(oAuth2Authentication);
        } catch (Exception e) {
            e.printStackTrace();
            AuthenticationFailureBadCredentialsEvent failureEvent = new AuthenticationFailureBadCredentialsEvent(null, new BadCredentialsException("免密登录失败"));
            eventPublisher.publishEvent(failureEvent);
            throw new GDBizException("免密登陆失败");
        }
    }

    private Authentication createAuthentication(GdUserDTO user) {
        // 创建一个无密码的 Authentication 对象
        return new UsernamePasswordAuthenticationToken(
                new Oauth2UserDetails(user),
                null, // 密码为空
                Lists.newArrayList() // 用户的权限
        );
    }

    private OAuth2Authentication createOAuth2Authentication(Authentication authentication) {
        // 从 ClientDetailsService 中加载客户端信息
        ClientDetails clientDetails = clientDetailsService.loadClientByClientId("biance");

        // 创建 OAuth2Request
        OAuth2Request oAuth2Request = new OAuth2Request(
                Collections.EMPTY_MAP, // 请求参数
                clientDetails.getClientId(),
                authentication.getAuthorities(),
                true, // 已经授权
                clientDetails.getScope(),
                clientDetails.getResourceIds(),
                null,
                null,
                null
        );

        // 构造 OAuth2Authentication
        return new OAuth2Authentication(oAuth2Request, authentication);
    }
}
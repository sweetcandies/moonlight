package com.funiverise.oauth.oauth2.support.service;


import com.guideir.common.base.GDCommonResult;
import com.guideir.common.base.GDRetCode;
import com.guideir.common.exception.GDBizException;
import com.guideir.common.redis.GDRedisTemplateUtil;
import com.guideir.oauth.oauth2.support.IpFilterStrategy;
import com.guideir.oauth.oauth2.support.Oauth2UserDetails;
import com.guideir.oauth.redis.OauthKeyConstant;
import com.guideir.system.common.constant.GdSystemConfigEnum;
import com.guideir.system.common.dto.GdSystemConfigDto;
import com.guideir.system.common.dto.GdUserDTO;
import com.guideir.system.common.dto.SecurityConfigDTO;
import com.guideir.system.common.in.LoginVO;
import com.guideir.system.feign.SystemFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

/**
 * @desc: 类的描述:认证服务器加载用户的类
 */
@Slf4j
@Service
public class Oauth2UserDetailService implements UserDetailsService {

    @Autowired
    public SystemFeignClient systemFeignClient;
    @Autowired
    private GDRedisTemplateUtil redisTemplateUtil;
    @Resource
    private IpFilterStrategy ipFilterStrategy;

    @Override
    public UserDetails loadUserByUsername(String username){
        // 获取缓存的剩余失败次数
        String key = OauthKeyConstant.LOGIN_FAIL_USER + username;
        Integer left = redisTemplateUtil.get(key);
        if (null != left && left == 0) {
            throw new GDBizException(GDRetCode.USERNAME_INVALID, "账户或密码不正确，尝试次数过多，请"+ redisTemplateUtil.getKeyValidTime(key) / 1000 + "秒后重试！");
        }
        SecurityConfigDTO securityConfig = getSecurityConfig();

        if (Objects.nonNull(securityConfig)
                && ipFilterStrategy.loginPreHand(securityConfig.getAccountLockTryCount(), securityConfig.getAccountLockTime())){
            throw new GDBizException("IP尝试次数过多，请稍后重试！");
        }
        //调用系统服务查询登录用户
        GdUserDTO result = systemFeignClient.loadUserByUsername(new LoginVO(username));
        if (null == result) {
            throw new UsernameNotFoundException("账户或密码不正确，请重新尝试");
        }
        return new Oauth2UserDetails(result);
    }

    public SecurityConfigDTO getSecurityConfig() {
        GdSystemConfigDto systemConfigDto = Optional.ofNullable(systemFeignClient.getConfigByType(GdSystemConfigEnum.SECURITY_CONFIG.getCode()))
                .map(GDCommonResult::getData).orElse(null);
        if (null == systemConfigDto || systemConfigDto.getSecurityConfig() == null ) {
            log.error("获取配置失败");
            return null;
        }
        return systemConfigDto.getSecurityConfig();
    }

}

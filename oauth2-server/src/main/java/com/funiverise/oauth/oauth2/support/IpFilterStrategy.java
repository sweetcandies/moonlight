package com.funiverise.oauth.oauth2.support;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.StrUtil;
import com.guideir.common.constant.CommonConstant;
import com.guideir.common.redis.GDRedisTemplateUtil;
import com.guideir.oauth.oauth2.ThreadLocalConstant;
import com.guideir.oauth.redis.OauthKeyConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: meiyang
 * @date: 2024/12/26 11:17
 * @version:
 * @Description:
 */
@Slf4j
@Component
public class IpFilterStrategy implements InitializingBean {
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(1, new NamedThreadFactory("fail-ip-clear", false));
    @Resource
    private GDRedisTemplateUtil redisTemplateUtil;
    /**
     * @author meiyang
     * @createTime 11:24 2024/12/26
     * @param 
     * @return
     * @description 前置拦截
     **/
    public boolean loginPreHand(Integer total, Long expireTime){
        String loginIp = queryCurrentIp();
        if (StrUtil.isBlank(loginIp)) return false;
        Set<String> failIpList = redisTemplateUtil.getSet(OauthKeyConstant.LOGIN_FAIL_IP);
        if (CollectionUtil.isEmpty(failIpList)) return false;
        Set<String> filterIps = failIpList.stream().filter(StrUtil::isNotBlank)
                .filter(ip -> ip.startsWith(loginIp))
                .filter(ip -> Objects.equals(ip.split(CommonConstant.AND).length, 3)).collect(Collectors.toSet());
        boolean filter = filterIps.stream().anyMatch(ip -> {
            int count = Integer.parseInt(ip.split(CommonConstant.AND)[1]);
            long time = Long.parseLong(ip.split(CommonConstant.AND)[2]);
            return count >= total && (System.currentTimeMillis() - time < (expireTime* 1000L));
        });
        if (!filter){
            filterIps.stream().filter(ip -> {
                int count = Integer.parseInt(ip.split(CommonConstant.AND)[1]);
                long time = Long.parseLong(ip.split(CommonConstant.AND)[2]);
                return count >= total && (System.currentTimeMillis() - time >= (expireTime * 1000L));
            }).forEach(ip -> redisTemplateUtil.removeSetValue(OauthKeyConstant.LOGIN_FAIL_IP, ip));
        }
        return filter;
    }
    /**
     * @author meiyang
     * @createTime 11:17 2024/12/26
     * @param 
     * @return
     * @description 登录失败处理
     **/
    public Integer loginFailProcess(){
        String loginIp = queryCurrentIp();
        if (StrUtil.isBlank(loginIp)) return 0;
        Set<String> failIpList = redisTemplateUtil.getSet(OauthKeyConstant.LOGIN_FAIL_IP);
        if (CollectionUtil.isNotEmpty(failIpList)){
            Set<String> filterIpItems = failIpList.stream().filter(StrUtil::isNotBlank).filter(ip -> ip.startsWith(loginIp))
                    .filter(ip -> Objects.equals(ip.split(CommonConstant.AND).length, 3))
                    .collect(Collectors.toSet());
            if (CollectionUtil.isNotEmpty(filterIpItems)){
                Set<String> renewIpItems = filterIpItems.stream().map(ip -> {
                    String realIP = ip.split(CommonConstant.AND)[0];
                    int count = Integer.parseInt(ip.split(CommonConstant.AND)[1]);
                    count++;
                    return realIP + CommonConstant.AND + count + CommonConstant.AND + System.currentTimeMillis();
                }).collect(Collectors.toSet());
                Integer failCount = 1;
                filterIpItems.forEach(ip -> redisTemplateUtil.removeSetValue(OauthKeyConstant.LOGIN_FAIL_IP, ip));
                for (String ip : renewIpItems){
                    failCount = Math.max(Integer.parseInt(ip.split(CommonConstant.AND)[1]), failCount);
                    redisTemplateUtil.addSet(OauthKeyConstant.LOGIN_FAIL_IP, ip);
                }
                return failCount;
            }else {
                String value = loginIp + CommonConstant.AND + 1 + CommonConstant.AND + System.currentTimeMillis();
                redisTemplateUtil.addSet(OauthKeyConstant.LOGIN_FAIL_IP, value);
                return 1;
            }
        }else {
            String value = loginIp + CommonConstant.AND + 1 + CommonConstant.AND + System.currentTimeMillis();
            redisTemplateUtil.addSet(OauthKeyConstant.LOGIN_FAIL_IP, value);
            return 1;
        }
    }
    /**
     * @author meiyang
     * @createTime 11:21 2024/12/26
     * @param 
     * @return
     * @description 登录成功
     **/
    public void loginSuccessProcess(String loginIp){
        if (StrUtil.isBlank(loginIp)) return;
        Set<String> failIpList = redisTemplateUtil.getSet(OauthKeyConstant.LOGIN_FAIL_IP);
        if (CollectionUtil.isNotEmpty(failIpList)){
            Set<String> filterIpItems = failIpList.stream().filter(StrUtil::isNotBlank).filter(ip -> ip.startsWith(loginIp))
                    .filter(ip -> Objects.equals(ip.split(CommonConstant.AND).length, 3))
                    .collect(Collectors.toSet());
            filterIpItems.forEach(ip -> redisTemplateUtil.removeSetValue(OauthKeyConstant.LOGIN_FAIL_IP, ip));
        }
    }

    private String queryCurrentIp(){
        if (Objects.isNull(ThreadLocalConstant.CURRENT_OAUTH_REQUEST.get()) || !ThreadLocalConstant.CURRENT_OAUTH_REQUEST.get().containsKey("current_login_ip")){
            log.error("current login ip query null");
            return null;
        }
        return (String) ThreadLocalConstant.CURRENT_OAUTH_REQUEST.get().get("current_login_ip");
    }

    /**
     * @author meiyang
     * @createTime 11:48 2024/12/26
     * @param 
     * @return
     * @description 锁定一天后，自动解锁
     **/
    private void clearExpireIp(){
        try {
            Set<String> failIpList = redisTemplateUtil.getSet(OauthKeyConstant.LOGIN_FAIL_IP);
            if (CollectionUtil.isEmpty(failIpList)) return;
            failIpList.stream()
                    .filter(ip -> Objects.equals(ip.split(CommonConstant.AND).length, 3))
                    .filter(ip -> {
                        long time = Long.parseLong(ip.split(CommonConstant.AND)[2]);
                        return System.currentTimeMillis() - time > 1000L * 60 * 60 * 24;
                    }).forEach(ip -> redisTemplateUtil.removeSetValue(OauthKeyConstant.LOGIN_FAIL_IP, ip));
        }catch (Exception e){
            log.error("", e);
        }
    }

    @Override
    public void afterPropertiesSet() {
        SCHEDULED_EXECUTOR.scheduleAtFixedRate(this::clearExpireIp, 30L, 15L, TimeUnit.MINUTES);
    }
}

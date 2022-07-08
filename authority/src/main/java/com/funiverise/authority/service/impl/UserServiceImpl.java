package com.funiverise.authority.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.funiverise.authority.dao.UserMapper;
import com.funiverise.authority.entity.Permission;
import com.funiverise.authority.entity.Role;
import com.funiverise.authority.entity.User;
import com.funiverise.authority.service.IPermissionService;
import com.funiverise.authority.service.IRoleService;
import com.funiverise.authority.service.IUserService;
import com.funiverise.constant.Constants;
import com.funiverise.enums.ReturnResultEnums;
import com.funiverise.message.ReturnMsg;
import com.funiverise.object.vo.UserDetailVO;
import com.funiverise.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IPermissionService permissionService;

    @Autowired
    private TokenEndpoint tokenEndpoint;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ClientDetailsService clientDetailsService;


    private static final String CLIENT_ID = "user-client";
    private static final String CLIENT_SECRET = "user-secret-8888";

    @Override
    public ReturnMsg<UserDetailVO> getUserDetail(String username) {
        if (StringUtils.isBlank(username)) {
            return ReturnMsg.initFailResult(ReturnResultEnums.R_000001);
        }
        User user = getUserByUsername(username);
        if (null == user) {
            return ReturnMsg.initFailResult(ReturnResultEnums.R_000002);
        }
        Set<Role> roles = roleService.getRoleSetByUsername(username);
        if (CollectionUtil.isEmpty(roles)) {
            log.debug("[用户角色为空]");
            return ReturnMsg.initFailResult(ReturnResultEnums.R_000003);
        }
        Set<Permission> permissions = permissionService.getPermissionsByRoleId(roles.stream().map(Role::getId).toArray(String[]::new));
        if (CollectionUtil.isEmpty(permissions)) {
            log.debug("[用户角色的权限为空]");
            return ReturnMsg.initFailResult(ReturnResultEnums.R_000003);
        }
        ReturnMsg<UserDetailVO> returnMsg = new ReturnMsg<>(true);

        Set<com.funiverise.object.pojo.Role> roleSet = roles.stream().map(role ->
                BeanUtils.copyProperties(role, com.funiverise.object.pojo.Role.class)).collect(Collectors.toSet());
        Set<com.funiverise.object.pojo.Permission> permissionSet = permissions.stream().map(permission ->
                BeanUtils.copyProperties(permission, com.funiverise.object.pojo.Permission.class)).collect(Collectors.toSet());

        UserDetailVO userDetailVO = UserDetailVO.builder().roleSet(roleSet).permissionSet(permissionSet).build();
        try {
            BeanUtils.copyProperties(user,userDetailVO,new String[0]);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.debug("[对象copy失败]");
            e.printStackTrace();
            returnMsg.setErrorMsg(ReturnResultEnums.R_000004.getDesc());
            return returnMsg;
        }
        returnMsg.setHasError(false);
        returnMsg.setResult(userDetailVO);
        returnMsg.setCode(ReturnResultEnums.SUCCESS.getCode());
        return returnMsg;
    }

    @Override
    public User getUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("username",username)
                .ge("DELETED", Constants.NOT_DELETED);
        return getOne(queryWrapper);
    }

    @Override
    public ReturnMsg<String> loginByPassword(String username, String password) {
        Map<String,String> parameters = new HashMap<>();
        parameters.put("grant_type","password");
        parameters.put("client_id",CLIENT_ID);
        parameters.put("client_secret", CLIENT_SECRET);
        parameters.put("scope","all");
        try {
            Principal principal = SecurityContextHolder.getContext().getAuthentication();
            ResponseEntity<OAuth2AccessToken> response =  tokenEndpoint.postAccessToken(principal, parameters);

        } catch (HttpRequestMethodNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 新增用户的通用方法
     * @param detailVO
     * @return
     */
    @Override
    public ReturnMsg<String> addNewUser(UserDetailVO detailVO) {
        ReturnMsg<String> returnMsg = new ReturnMsg<>(true);
        log.info("用户注册");

        return null;
    }


}

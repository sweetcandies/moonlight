package com.funiverise.gateway.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.funiverise.constant.Constants;
import com.funiverise.constant.MessageConstant;
import com.funiverise.constant.ResultConstant;
import com.funiverise.gateway.dao.UserMapper;
import com.funiverise.gateway.entity.User;
import com.funiverise.gateway.service.IPermissionService;
import com.funiverise.gateway.service.IRoleService;
import com.funiverise.gateway.service.IUserService;
import com.funiverise.message.ReturnMsg;
import com.funiverise.object.vo.UserDetailVO;
import com.funiverise.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

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

    @Override
    public ReturnMsg<UserDetailVO> getUserDetail(String username) {
        if (StringUtils.isBlank(username)) {
            return ReturnMsg.initFailResult(MessageConstant.RS_00001);
        }
        User user = getUserByUsername(username);
        if (null == user) {
            return ReturnMsg.initFailResult(MessageConstant.RS_00002);
        }
        Set<com.funiverise.object.pojo.Role> roles = roleService.getRoleSetByUsername(username);
        if (CollectionUtil.isEmpty(roles)) {
            log.debug("[用户角色为空]");
            return ReturnMsg.initFailResult(MessageConstant.RS_00003);
        }
        Set<com.funiverise.object.pojo.Permission> permissions = permissionService.getPermissionsByRoleId(roles.stream().map(com.funiverise.object.pojo.Role::getId).toArray(String[]::new));
        if (CollectionUtil.isEmpty(permissions)) {
            log.debug("[用户角色的权限为空]");
            return ReturnMsg.initFailResult(MessageConstant.RS_00003);
        }
        ReturnMsg<UserDetailVO> returnMsg = new ReturnMsg<>(true);
        UserDetailVO userDetailVO = UserDetailVO.builder().roleSet(roles).permissionSet(permissions).build();
        try {
            BeanUtils.copyProperties(user,userDetailVO,new String[0]);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.debug("[对象copy失败]");
            e.printStackTrace();
            returnMsg.setErrorMsg(MessageConstant.RS_00004);
            return returnMsg;
        }
        returnMsg.setHasError(false);
        returnMsg.setResult(userDetailVO);
        returnMsg.setCode(ResultConstant.SUCCESS);
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


}

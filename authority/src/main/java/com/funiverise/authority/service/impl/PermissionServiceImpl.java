package com.funiverise.authority.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.funiverise.authority.dao.PermissionMapper;
import com.funiverise.authority.entity.Permission;
import com.funiverise.authority.service.IPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * 权限表 服务实现类
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    @Autowired
    private PermissionMapper permissionMapper;


    @Override
    public Set<com.funiverise.object.pojo.Permission> getPermissionsByRoleId(String[] roleIds) {
        Set<com.funiverise.object.pojo.Permission> permissions = new HashSet<>();
        if (null == roleIds || roleIds.length == 0) {
            return permissions;
        }
        Optional.ofNullable(permissionMapper.selectRolePermissionsById(roleIds)).ifPresent(permissions::addAll);
        return permissions;
    }
}

package com.funiverise.authority.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.funiverise.authority.entity.Permission;

import java.util.Set;

/**
 * <p>
 * 权限表 服务类
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
public interface IPermissionService extends IService<Permission> {

    Set<com.funiverise.object.pojo.Permission> getPermissionsByRoleId(String[] roleId);
}

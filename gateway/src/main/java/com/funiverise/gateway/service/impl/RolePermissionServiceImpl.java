package com.funiverise.gateway.service.impl;

import com.funiverise.gateway.entity.RolePermission;
import com.funiverise.gateway.dao.RolePermissionMapper;
import com.funiverise.gateway.service.IRolePermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色权限关联表 服务实现类
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements IRolePermissionService {

}

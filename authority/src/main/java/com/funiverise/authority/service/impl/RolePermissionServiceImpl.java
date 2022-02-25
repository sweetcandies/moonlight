package com.funiverise.authority.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.funiverise.authority.dao.RolePermissionMapper;
import com.funiverise.authority.entity.RolePermission;
import com.funiverise.authority.service.IRolePermissionService;
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

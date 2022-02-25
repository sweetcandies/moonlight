package com.funiverise.gateway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.funiverise.gateway.entity.Role;

import java.util.Set;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
public interface IRoleService extends IService<Role> {

    Set<com.funiverise.object.pojo.Role> getRoleSetByUsername(String username);
}

package com.funiverise.authority.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.funiverise.authority.dao.RoleMapper;
import com.funiverise.authority.entity.Role;
import com.funiverise.authority.service.IRoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Set<com.funiverise.object.pojo.Role> getRoleSetByUsername(String username) {
        Set<com.funiverise.object.pojo.Role> roles = new HashSet<>();
        if (StringUtils.isBlank(username)) {
            return roles;
        }
        Optional.ofNullable(roleMapper.selectUserRolesByUsername(username)).ifPresent(roles::addAll);
        return roles;
    }
}

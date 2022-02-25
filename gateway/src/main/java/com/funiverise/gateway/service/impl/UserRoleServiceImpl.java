package com.funiverise.gateway.service.impl;

import com.funiverise.gateway.entity.UserRole;
import com.funiverise.gateway.dao.UserRoleMapper;
import com.funiverise.gateway.service.IUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户角色关联表 服务实现类
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

}

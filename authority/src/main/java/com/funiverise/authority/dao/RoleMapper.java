package com.funiverise.authority.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.funiverise.authority.entity.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    List<Role> selectUserRolesByUsername(String username);
}

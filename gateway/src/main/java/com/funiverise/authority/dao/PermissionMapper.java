package com.funiverise.authority.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.funiverise.authority.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 权限表 Mapper 接口
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    List<com.funiverise.object.pojo.Permission> selectRolePermissionsById(String[] roleIds);
}

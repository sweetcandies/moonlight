package com.funiverise.gateway.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 角色权限关联表
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@TableName("t_role_permission")
@Builder
@Data
public class RolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String roleId;

    private String permissionId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String creatorId;

    private String modifierId;

    private Integer deleted;

    @Override
    public String toString() {
        return "RolePermission{" +
        "id=" + id +
        ", roleId=" + roleId +
        ", permissionId=" + permissionId +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        ", creatorId=" + creatorId +
        ", modifierId=" + modifierId +
        "}";
    }
}

package com.funiverise.authority.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@TableName("t_role")
@Builder
@Data
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String code;

    private String parentId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String creatorId;

    private String modifierId;

    private Integer deleted;

    @Override
    public String toString() {
        return "Role{" +
        "id=" + id +
        ", name=" + name +
        ", code=" + code +
        ", parentId=" + parentId +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        ", creatorId=" + creatorId +
        ", modifierId=" + modifierId +
        "}";
    }
}

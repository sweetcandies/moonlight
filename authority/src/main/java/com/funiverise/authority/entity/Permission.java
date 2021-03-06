package com.funiverise.authority.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * ๆ้่กจ
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@TableName("t_permission")
@Builder
@Data
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String code;

    private String name;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String creatorId;

    private String modifierId;

    private Integer deleted;

    @Override
    public String toString() {
        return "Permission{" +
        "id=" + id +
        ", code=" + code +
        ", name=" + name +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        ", creatorId=" + creatorId +
        ", modifierId=" + modifierId +
        "}";
    }
}

package com.funiverise.gateway.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@TableName("t_user")
@Builder
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String username;

    private String password;

    private String mobile;

    private String email;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String creatorId;

    private String modifierId;

    private Integer deleted;

    private Integer enabled;

    private Integer status;



    @Override
    public String toString() {
        return "User{" +
        "id=" + id +
        ", username=" + username +
        ", password=" + password +
        ", mobile=" + mobile +
        ", email=" + email +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        ", creatorId=" + creatorId +
        ", modifierId=" + modifierId +
        ", enabled=" + enabled +
        ", status=" + status +
                "}";
    }
}

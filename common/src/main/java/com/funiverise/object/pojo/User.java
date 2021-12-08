package com.funiverise.object.pojo;

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
@Data
@Builder
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

    private String updatorId;

}

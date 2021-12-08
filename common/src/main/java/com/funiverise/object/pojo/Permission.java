package com.funiverise.object.pojo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 权限表
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
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

    private String updatorId;

}

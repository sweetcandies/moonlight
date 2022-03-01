package com.funiverise.object.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 权限表
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@Data
public class Permission {


    private String id;

    private String code;

    private String name;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String creatorId;

    private String modifierId;

}

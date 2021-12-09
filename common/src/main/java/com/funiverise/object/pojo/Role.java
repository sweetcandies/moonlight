package com.funiverise.object.pojo;

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
@Data
@Builder
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

}

package com.funiverise.object.pojo;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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
public class Role {


    private String id;

    private String name;

    private String code;

    private String parentId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String creatorId;

    private String modifierId;


    public Role () {}
    /**
     * @description: 重写code获取方法， 从而使role前自动带有Role标识
     * @param: []
     * @return: java.lang.String
     * @author: Funny
     * @date: 2021/12/13 11:35
     */
    public String getCode () {
        if (StringUtils.isNotBlank(this.code)) {
            return "ROLE_".concat(this.code);
        }
        return this.code;
    }


}

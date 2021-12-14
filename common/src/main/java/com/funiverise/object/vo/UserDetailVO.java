package com.funiverise.object.vo;

import com.funiverise.object.pojo.Permission;
import com.funiverise.object.pojo.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * @author Funny
 * @version 1.0
 * @description: TODO
 * @date 2021/12/8 15:47
 */
@Data
@Builder
public class UserDetailVO {

    private String id;

    private String username;

    private String password;

    private String mobile;

    private String email;

    private Set<Role> roleSet;

    private Set<Permission> permissionSet;
    /**1：启用；0：停用*/
    private Integer enabled;

    private Integer status;

    public Boolean isEnable() {
        if (null != enabled) {
            return this.enabled.equals(1);
        }
        return false;
    }
}

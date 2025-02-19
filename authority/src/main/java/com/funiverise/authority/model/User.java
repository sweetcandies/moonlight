package com.funiverise.authority.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @description:
 * @author: hanyuefan
 * @version: [1.0.0, 2025/02/19]
 */
@Data
@TableName("t_user")
@ApiModel(value = "User查询对象", description = "User查询对象")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @TableId
    private Long userId;
    private String username;
    private String password;
    private String realName;
    private String orgId;
    private String userMail;
    private String userMobile;
    private Date validEndTime;
    private Date validStartTime;
    private String lastLoginIp;
    private String lastLoginBrowser;
    private Date lastLoginTime;
    private Integer gender;
    private Date created;
    private Date modified;
    private Integer userStatus;
}

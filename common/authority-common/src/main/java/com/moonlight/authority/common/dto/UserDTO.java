package com.moonlight.authority.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import com.moonlight.authority.common.out.MenuOutVO;
import com.moonlight.authority.common.out.RoleOutVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: hanyuefan
 * @version: [1.0.0, 2024/01/24]
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    @ApiModelProperty("登陆账户")
    private String username;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("真实姓名")
    private String realName;
    @ApiModelProperty("组织机构id")
    private String orgId;
    @ApiModelProperty("组织机构名称")
    private String orgName;
    @ApiModelProperty("邮箱")
    private String userMail;
    @ApiModelProperty("手机号")
    private String userMobile;
    @ApiModelProperty("用户过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validEndTime;
    @ApiModelProperty("用户激活时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validStartTime;
    @ApiModelProperty("上次登录ip")
    private String lastLoginIp;
    @ApiModelProperty("上次登录浏览器")
    private String lastLoginBrowser;
    @ApiModelProperty("性别：0-女，1-男")
    private Integer gender;
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created;
    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modified;
    @ApiModelProperty("用户状态：1=启用")
    private Integer userStatus;
    @ApiModelProperty("用户权限codes")
    private List<String> authorities;
    // 菜单id
    @ApiModelProperty("菜单id")
    private List<MenuOutVO> menus;
    /**
     * 角色权限
     */
    @ApiModelProperty("角色权限")
    private List<RoleOutVO> roles;

    @ApiModelProperty("上次登陆时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;


    
}

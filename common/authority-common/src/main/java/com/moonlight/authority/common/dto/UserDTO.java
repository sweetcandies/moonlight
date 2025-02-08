package com.moonlight.authority.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

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
    private List<GdMenuOutVO> menus;
    /**
     * 角色权限
     */
    @ApiModelProperty("角色权限")
    private List<GdRoleOutVO> roles;
    @ApiModelProperty("平台信息")
    private GdPlatformInfoOutVO platform;

    private String token;
    @ApiModelProperty("是否编辑过根节点组织机构，1、已编辑，0、未编辑")
    private Integer isRootOrgEdited;
    @ApiModelProperty("下级用户打开主页")
    private String lowerOpen;
    @ApiModelProperty("是否为超管用户")
    private Integer isSuperAdmin;
    @ApiModelProperty("组织机构层级")
    private Integer orgLevel;
    @ApiModelProperty("上次登陆时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;
    @ApiModelProperty("用户最高角色等级")
    private Integer userRoleLevel;
    @ApiModelProperty("用户最低角色等级")
    private Integer lowestRoleLevel;

    @ApiModelProperty("系统设置-全局功能-守望位是否开启")
    private boolean watchPositionEnabled;
    @ApiModelProperty("系统设置-全局功能-守望位策略，默认1表示平台控制，2表示设备控制")
    private Integer watchPositionStrategy;
    @ApiModelProperty("巡视策略，默认1表示自动配置，2表示表示手动配置")
    private Integer inspectionPointStrategy = 1;
    @ApiModelProperty("是否需要修改密码")
    private boolean passwordChangeStatus;
    @ApiModelProperty("会话有效期")
    private Long sessionValidity;
    @ApiModelProperty("上次修改密码时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastModifyPwdDate;
    @ApiModelProperty("剩余登录失败次数")
    private Integer leftFailedTimes;
    @ApiModelProperty("是否启用服务端超分计算")
    private Boolean enableServerSuperAnalysis;
    @ApiModelProperty("用户来源 1：边侧系统")
    private Integer userSource = 0;
    
}

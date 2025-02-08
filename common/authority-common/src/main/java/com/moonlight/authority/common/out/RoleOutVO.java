package com.moonlight.authority.common.out;

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
 * @version: [1.0.0, 2024/02/06]
 */
@Data
public class RoleOutVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("角色主键id")
    private Long roleId;
    @ApiModelProperty("角色名称")
    private String roleName;
    @ApiModelProperty("角色类型")
    private Integer roleType;
    @ApiModelProperty("角色级别")
    private Integer roleLevel;
    @ApiModelProperty("描述")
    private String roleDescription;
    @ApiModelProperty("角色状态1启用，0禁用")
    private Integer roleStatus;
    @ApiModelProperty("下级打开页面")
    private String lowerOpen;
    @ApiModelProperty("用户数")
    private Long userCount;
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = DateFormatConstant.DATE_FORMAT, timezone = "GMT+8")
    private Date created;
    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = DateFormatConstant.DATE_FORMAT, timezone = "GMT+8")
    private Date modified;
    @ApiModelProperty("仅有次角色用户数")
    private Integer hasOnlyRoleUserNum;

    private List<GdAuthOutVO> authList;
}

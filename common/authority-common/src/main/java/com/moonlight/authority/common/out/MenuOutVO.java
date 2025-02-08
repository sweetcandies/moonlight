package com.moonlight.authority.common.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author jiangpf
 * @version [1.0.0, 2024/02/20]
 * @description
 */
@Data
public class MenuOutVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "菜单id")
    private Long menuId;

    @ApiModelProperty(value = "菜单名称")
    private String menuName;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "图标url")
    private String iconUrl;

    @ApiModelProperty(value = "菜单url")
    private String menuUrl;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "上级菜单id")
    private Long menuPid;

    @ApiModelProperty(value = "排序优先级，0最高")
    private Integer sortLevel;

    @ApiModelProperty(value = "菜单状态：0、都不可见，1、都可见，2、仅中心可见，3、仅边缘可见")
    private Integer menuEnable;

    @ApiModelProperty(value = "删除标记:0未删除,1已删除")
    private Boolean deleted;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modified;

}

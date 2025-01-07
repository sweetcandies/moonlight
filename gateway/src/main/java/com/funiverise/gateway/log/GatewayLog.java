package com.funiverise.gateway.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * mingrt001
 * 20240709
 */
@Data
public class GatewayLog implements Serializable {

    private static final long serialVersionUID = -3205904134722576668L;
    /**
     * 主键id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String userName;

    /**
     * 组织编码
     */
    @ApiModelProperty("组织编码")
    private String orgCode;

    /**
     * 组织名称
     */
    @ApiModelProperty("组织名称")
    private String orgName;

    /**
     * 操作模块
     */
    @ApiModelProperty("操作模块")
    private Integer module;

    /**
     * 操作模块名称
     */
    @ApiModelProperty("操作模块名称")
    private String moduleName;

    /**
     * 操作类型
     */
    @ApiModelProperty("操作类型")
    private Integer type;

    /**
     * 操作类型名称
     */
    @ApiModelProperty("操作类型名称")
    private String typeName;

    /**
     * 行为
     */
    @ApiModelProperty("行为")
    private Integer action;

    /**
     * 行为名称
     */
    @ApiModelProperty("行为名称")
    private String actionName;

    /**
     * 访问实例
     */
    @ApiModelProperty("访问实例")
    private String targetServer;

    /**
     * 请求路径
     */
    @ApiModelProperty("请求路径")
    private String requestPath;

    /**
     * 请求方式
     */
    @ApiModelProperty("请求方式")
    private String method;

    /**
     * 请求协议
     */
    @ApiModelProperty("请求协议")
    private String schema;

    /**
     * 请求ip
     */
    @ApiModelProperty("客户端ip")
    private String ip;

    /**
     * 请求时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("请求时间")
    private Date requestTime;

    /**
     * 请求参数
     */
    @ApiModelProperty("请求参数")
    private String queryParams;

    /**
     * 请求体
     */
    @ApiModelProperty("请求体")
    private String requestBody;

    /**
     * 请求执行时间
     */
    @ApiModelProperty("请求执行时间，单位ms")
    private Long executeTime;

    /**
     * 请求类型
     */
    @ApiModelProperty("请求类型")
    private String requestContentType;

    /**
     * 响应状态码
     */
    @ApiModelProperty("响应状态码")
    private int code;

    /**
     * 响应体
     */
    @ApiModelProperty("响应体")
    private String responseBody;
}
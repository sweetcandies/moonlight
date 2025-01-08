package com.funiverise.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: meiyang
 * @date: 2024年01月22日 17:13:56
 * @version:
 * @Description:
 */
@Getter
@AllArgsConstructor
public enum RetCode {
    SUCCESS("操作成功", 200),
    ACCEPTED("操作进行中", 202),
    BAD_REQUEST("请求参数不正确", 400),
    UNAUTHORIZED("无效token", 401),
    FORBIDDEN("没有该操作权限", 403),
    NOT_FOUND("没有找到资源", 404),
    METHOD_NOT_ALLOWED("不支持的请求方式", 405),
    REQUEST_TIMEOUT("请求超时", 408),
    CONNECT_TIMEOUT("请求级联超时", 409),
    TOO_MANY_REQUESTS("请求过于频繁，请稍后重试", 429),
    FAILED("操作失败", 500),
    BAD_GATEWAY("网关服务异常", 502),
    SERVICE_NOT_FOUND("服务未启动", 503),



    MSG_NOT_RET("特殊标识", -1),


    MSG_INVALID("消息内容无效", 10002),

    //    ADMIN_ADD_ERROR ("企业用户无法添加",20002) ,

    //    ADMIN_UPDATE_ERROR ("企业用户无法编辑",20003) ,

    MSG_PARAM_NULL("消息字段为空", 10003),

    USERNAME_INVALID("无效用户名", 10004),

    NO_AUTH("未鉴权", 10005),

    PASSWORD_FAILD("密码修改失败", 10006),

    NO_PROMIS("用户无权限操作", 10007),

    HANDLE_FALD("用户操作失败", 10008),

    PASSWORD_ERROR("密码错误", 10009),

    USER_LOCKED("用户锁定", 10010),

    ;

    // 成员变量
    private final String message;
    private final Integer code;


    public boolean harError() {
        return !SUCCESS.code.equals(this.code);
    }
}

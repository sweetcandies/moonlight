package com.funiverise.enums;

/**
 * @author Funny
 * @version 1.0
 * @description: 返回结果枚举类
 * @date 2021/12/6 17:27
 */

public enum ReturnResultEnums {

    SUCCESS("000000", "成功"),
    FAILURE("999999", "失败"),
    R_000001("000001","用户名不可为空"),
    R_000002("000002","用户不存在"),
    R_000003("000003","用户信息残缺，请联系管理员"),
    R_000004("000004","系统内部错误，请联系管理员"),
    R_000005("000005", "用户名或密码不可为空！"),
    R_000006("000006","远程调用错误！");

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    private String code;
    private String desc;


    ReturnResultEnums(String code,String desc) {
        this.code = code;
        this.desc = desc;
    }


}

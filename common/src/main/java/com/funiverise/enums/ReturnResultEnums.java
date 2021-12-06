package com.funiverise.enums;

import com.funiverise.constant.ResultConstant;
import lombok.Data;

/**
 * @author Funny
 * @version 1.0
 * @description: 返回结果枚举类
 * @date 2021/12/6 17:27
 */

public enum ReturnResultEnums {

    SUCCESS("000000", ResultConstant.SUCCESS),
    FAILURE("999999", ResultConstant.FAILURE);

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    private String code;
    private String desc;


    ReturnResultEnums(String code,String desc) {
        this.code = code;
        this.desc = desc;
    }

}

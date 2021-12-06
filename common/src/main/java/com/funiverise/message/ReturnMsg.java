package com.funiverise.message;

import java.time.LocalTime;
import com.funiverise.constant.TimeFormatConstant;
import com.funiverise.enums.ReturnResultEnums;
import com.funiverise.utils.TimeUtils;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Funny
 * @version 1.0
 * @description: 返回信息公用类
 * @date 2021/12/6 17:16
 */
@Data
@NoArgsConstructor
public class ReturnMsg<T> {

    private String code;

    private String message;

    private String time;

    private Boolean hasError;

    private T result;

    private String errorMsg;

    public ReturnMsg(Boolean hasError) {
        this.hasError = hasError;
        this.time = TimeUtils.getFormatTime(LocalTime.now(), TimeFormatConstant.YYYY_MM_DD_LONG);
    }

    public ReturnMsg(String code, String message) {
        this.code = code;
        this.message = message;
        this.hasError = false;
        this.time = TimeUtils.getFormatTime(LocalTime.now(), TimeFormatConstant.YYYY_MM_DD_LONG);
    }

    public static ReturnMsg<?> initSuccessResult(String message) {
        return new ReturnMsg<>(ReturnResultEnums.SUCCESS.getCode(),message);
    }

    public static ReturnMsg<?> initFailResult(String message) {
        ReturnMsg<?> returnMsg = new ReturnMsg<>(true);
        returnMsg.setErrorMsg(message);
        return returnMsg;
    }

    public static ReturnMsg<?> getReturnMsg(String code ,String message) {
        return new ReturnMsg<>(code,message);
    }

}

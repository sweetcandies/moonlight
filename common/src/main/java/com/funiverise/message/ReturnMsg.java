package com.funiverise.message;

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
        this.time = TimeUtils.getFormatTime(System.currentTimeMillis(), TimeFormatConstant.YYYY_MM_DD_LONG);
    }

    public ReturnMsg(String code, String message) {
        this.code = code;
        this.message = message;
        this.hasError = false;
        this.time = TimeUtils.getFormatTime(System.currentTimeMillis(), TimeFormatConstant.YYYY_MM_DD_LONG);
    }

    public static <T> ReturnMsg<T> initSuccessResult(String message) {
        return new ReturnMsg<>(ReturnResultEnums.SUCCESS.getCode(),message);
    }

    public static <T> ReturnMsg<T> initFailResult(ReturnResultEnums result) {
        ReturnMsg<T> returnMsg = new ReturnMsg<>(true);
        returnMsg.setCode(result.getCode());
        returnMsg.setErrorMsg(result.getDesc());
        return returnMsg;
    }

    public static <T> ReturnMsg<T> getReturnMsg(String code ,String message) {
        return new ReturnMsg<>(code,message);
    }


    public Boolean isHasError() {
        return this.hasError;
    }
}

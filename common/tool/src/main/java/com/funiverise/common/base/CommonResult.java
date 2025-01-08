package com.funiverise.common.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.funiverise.common.enums.RetCode;

/**
 * @Author: meiyang
 * @date: 2024年01月22日 17:51:16
 * @version:
 * @Description:
 */
public class CommonResult<T> {

    public static final String OK_CODE = "0";
    public static final String OK_MSG = "成功";
    public static final String GENERAL_ERROR_CODE = "-1";
    public static final String GENERAL_ERROR_MSG = "失败";
    private String code;
    private String msg;
    private T data;
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","fieldHandler","mostSpecificCause"})
    private Throwable throwable;

    public CommonResult() {
    }

    public CommonResult(String code, String msg, T d, Throwable throwable) {
        this.code = code;
        this.msg = msg;
        this.data = d;
        this.throwable = throwable;
    }

    public static <T> CommonResult<T> ok(T d) {
        return new CommonResult(OK_CODE, OK_MSG, d, null);
    }

    public static <T> CommonResult<T> ok() {
        return ok(null);
    }

    public static <T> CommonResult<T> error(Throwable throwable) {
        return error(GENERAL_ERROR_MSG, throwable);
    }

    public static <T> CommonResult<T> error(String message) {
        return error(GENERAL_ERROR_CODE, message);
    }

    public static <T> CommonResult<T> error(String message, Throwable throwable) {
        return new CommonResult(GENERAL_ERROR_CODE, message, null, throwable);
    }

    public static <T> CommonResult<T> error(RetCode code) {
        return new CommonResult<>(code.getCode().toString(), code.getMessage(), null, null);
    }

    public static <T> CommonResult<T> error(String code, String error) {
        return new CommonResult(code, error, null, null);
    }

    public boolean success() {
        return OK_CODE.equals(this.code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}

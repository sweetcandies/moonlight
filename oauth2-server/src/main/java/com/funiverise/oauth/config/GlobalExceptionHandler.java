package com.funiverise.oauth.config;

import com.guideir.common.base.GDCommonResult;
import com.guideir.common.base.GDRetCode;
import com.guideir.common.exception.GDBizException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *@author wshun
 *@date 2022/09/22  14:54:42
 *@description  全局异常处理
 *@version 1.0.0
 */
@ControllerAdvice
@SuppressWarnings("all")
public class GlobalExceptionHandler {

    /**
     * 捕获全局Exception异常,解析异常
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GDCommonResult handleException(Exception e){
        e.printStackTrace();
        //解析异常信息,如果是系统自定义异常直接返回信息
        if(e instanceof GDBizException){
            //解析系统自定义异常信息
            GDBizException exception= (GDBizException) e;
            if (exception.getResultCode() != null) {
                return GDCommonResult.error(exception.getResultCode());
            }
            return GDCommonResult.error(e.getMessage());
        }
        //统一定义为9999系统未知错误
        return  GDCommonResult.error(GDRetCode.SYSTEM_ERROR);
    }
}

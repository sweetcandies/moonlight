package com.funiverise.message;

import java.lang.;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Funny
 * @version 1.0
 * @description: 返回信息公用类
 * @date 2021/12/6 17:16
 */
@Data
public class ReturnMsg<T> {

    private String code;

    private String message;

    private String time;

    public ReturnMsg(String code, String message) {
        this.code = code;
        this.message = message;
        this.time =
    }

    public static ReturnMsg<?> initSuccessResult() {
        ReturnMsg<?> returnMsg = new ReturnMsg<>();
    }

}

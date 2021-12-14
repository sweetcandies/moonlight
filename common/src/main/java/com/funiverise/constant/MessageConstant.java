package com.funiverise.constant;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author Funny
 * @version 1.0
 * @description: TODO
 * @date 2021/12/8 16:57
 */
public class MessageConstant {

    private final String code;
    private final String msg;

    public MessageConstant(String code,String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode (String code) {
        return this.code;
    }

    public String getMsg(String msg) {
        return this.msg;
    }

    private static final Map<String,String> MESSAGE_MAP = ImmutableMap.<String, String>builder()
            .build();

    /**
     * @description: 加载所有常量至map
     * @param: []
     * @return: void
     * @author: Funny
     * @date: 2021/12/13 14:24
     */
    private static void initMessageMap() {

    }

}

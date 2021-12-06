package com.funiverise.constant;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Funny
 * @version 1.0
 * @description: 时间格式化常量
 * @date 2021/12/6 18:04
 */
public class TimeFormatConstant {

    /**年*/
    public static final String YYYY = "yyyy";
    /**月*/
    public static final String MM = "MM";
    /**日*/
    public static final String DD = "dd";
    /**时*/
    public static final String HH = "HH";
    /**分*/
    public static final String mm = "mm";
    /**秒*/
    public static final String ss = "ss";
    /**年-月*/
    public static final String YYYY_MM = "yyyy-MM";
    /**年-月-日*/
    public static final String YYYY_MM_DD = "yyyy-MM_dd";
    /**年-月-日 时:分:秒*/
    public static final String YYYY_MM_DD_LONG = "yyyy-MM-dd HH:mm:ss";


    private TimeFormatConstant(){}


    public static  <T>  String getFormatTime(T time, String pattern) {
        if (StringUtils.isBlank(pattern)) {
            throw new NullPointerException("时间格式不可为空");
        }
        if (null == time) {
            throw new NullPointerException("时间对象不可为空");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        if (time instanceof Date) {
            return formatter.format(((Date) time).toInstant());
        }
        if (time instanceof Calendar) {
            return formatter.format(((Calendar) time).toInstant());
        }
        if (time instanceof Long) {
            return 
        }
        return ;
    }



}

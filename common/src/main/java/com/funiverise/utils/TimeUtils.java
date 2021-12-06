package com.funiverise.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Funny
 * @Email hanyuefan776@163.com
 */
public class TimeUtils {
    /**
     * @Author hanyuefan
     * @Description  时间格式化
     * @Date 20:38 2021/12/6
     * @Param [time, pattern]
     * @return java.lang.String
     **/
    public static <T> String getFormatTime(T time, String pattern) {
        if (StringUtils.isBlank(pattern)) {
            throw new NullPointerException("时间格式不可为空");
        }
        if (null == time) {
            throw new NullPointerException("时间对象不可为空");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        if (time instanceof Date) {
            return formatter.format(((Date) time).toInstant());
        } else if (time instanceof Calendar) {
            return formatter.format(((Calendar) time).toInstant());
        } else if (time instanceof Long) {
            return formatter.format(TimeUtils.transLongToDate((Long) time).toInstant());
        }
        return null;
    }

    /**
     * @Author hanyuefan
     * @Description  将long型日期转换成Date类型
     * @Date 20:26 2021/12/6
     * @Param [number]
     * @return java.util.Date
     **/
    public static Date transLongToDate(Long number) {
        if (number == null) {
            throw new NullPointerException("日期数字不可为空");
        }
        return new Date(number);
    }

    /**
     * @Author hanyuefan
     * @Description  将date转long
     * @Date 20:33 2021/12/6
     * @Param [time]
     * @return java.lang.Long
     **/
    public static Long transDateToLong(Date time) {
        return time.getTime() / 1000;
    }
}

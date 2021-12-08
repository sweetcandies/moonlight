package com.funiverise.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Funny
 * @version 1.0
 * @description: TODO
 * @date 2021/12/8 14:57
 */
public class BeanUtils {

    private BeanUtils() {}

    /**
     * @description:  将对象转换成map
     * @param: [obj]
     * @return: java.util.Map<java.lang.String,?>
     * @author: Funny
     * @date: 2021/12/8 15:16
     */
    public static <T> Map<String,Object> entity2Map(T obj) {
        Map<String,Object> resultMap = new HashMap<>();
        if (null == obj) {
            return resultMap;
        }
        Arrays.stream(obj.getClass().getDeclaredFields()).forEach(field -> {
            try {
                resultMap.put(field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return resultMap;
    }

    /**
     * @description: 将source与target的同名同类型属性，从source复制到target，两种对象可以是不同类型
     * @param: [source, target, ignoreField]
     * @return: void
     * @author: Funny
     * @date: 2021/12/8 17:43
     */
    public static <T,E> void copyProperties(T source, E target, String[] ignoreField) throws NoSuchFieldException, IllegalAccessException {

        if (null == source || null == target)  {
            throw new NullPointerException("复制源或目标源对象不可为空！");
        }
        Field[] sourceFields = source.getClass().getDeclaredFields();
        Field[] targetFields = target.getClass().getDeclaredFields();
        for (Field t: targetFields) {
            // ignoreField为忽略比较的字段名称
            if (Arrays.stream(ignoreField).anyMatch(i->i.equals(t.getName()))) continue;
            // 如果目标的属性的名称、修饰符和 类型都相同，则认为这是两个相同的属性，就把源的值赋给目标

            if (Arrays.stream(sourceFields).anyMatch(s -> s.getName().equals(t.getName()) &&
                    s.getGenericType().equals(t.getGenericType()) && s.getModifiers() == t.getModifiers() )) {
                t.set(target,source.getClass().getDeclaredField(t.getName()));
            }
        }
    }

}

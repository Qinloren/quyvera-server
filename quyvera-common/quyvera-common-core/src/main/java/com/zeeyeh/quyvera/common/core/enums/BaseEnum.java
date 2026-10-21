package com.zeeyeh.quyvera.common.core.enums;

/**
 * 基础枚举接口
 * @author Qinloren
 * @date 2026/10/19 23:30
 */
public interface BaseEnum<T> {

    /**
     * 获取枚举值
     * @return 枚举值
     */
    T getValue();

    /**
     * 获取枚举描述
     * @return 枚举描述
     */
    String getDescription();

    /**
     * 获取枚举类型
     * @param enumClass 枚举类
     * @param value 目标值
     * @return 枚举对象
     * @param <E> 枚举类型
     * @param <T> 枚举值类型
     */
    static <E extends Enum<E> & BaseEnum<T>, T> E fromValue(Class<E> enumClass, T value) {
        if (value == null) {
            return null;
        }
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getValue().equals(value)) {
                return enumConstant;
            }
        }
        return null;
    }
}

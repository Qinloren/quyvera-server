package com.zeeyeh.quyvera.common.core.constant;

/**
 * 错误码、消息定义
 * @author Qinloren
 * @date 2026/10/20 15:01
 */
public interface ErrorCode {

    /**
     * 获取错误码
     * @return 错误码
     */
    Integer getCode();

    /**
     * 获取错误消息
     * @return 错误消息
     */
    String getKey();

    /**
     * 获取枚举类型
     * @param enumClass 枚举类
     * @param code 目标值
     * @return 枚举对象
     * @param <E> 枚举类型
     * @param <T> 枚举值类型
     */
    static <E extends Enum<E> & ErrorCode, T> E fromCode(Class<E> enumClass, Integer code) {
        if (code == null) {
            return null;
        }
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getCode().equals(code)) {
                return enumConstant;
            }
        }
        return null;
    }
}

package com.zeeyeh.quyvera.common.core.utils;

import com.zeeyeh.quyvera.common.core.constant.ErrorCode;
import com.zeeyeh.quyvera.common.core.exception.BusinessExceptions;

import java.util.function.Supplier;

/**
 * 业务断言工具类
 * @author Qinloren
 * @date 2026/10/20 15:42
 */
public class BusinessAsserts {
    private BusinessAsserts() {}

    /**
     * 业务断言
     * @param condition 条件
     * @param exceptionSupplier 异常提供者
     */
    public static void check(boolean condition, Supplier<? extends BusinessExceptions> exceptionSupplier) {
        if (condition) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 业务断言
     * @param condition 条件
     * @param errorCode 错误定义
     */
    public static void check(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BusinessExceptions(errorCode);
        }
    }

    /**
     * 业务断言
     * @param condition 条件
     * @param code 错误码
     * @param key 错误消息键
     */
    public static void check(boolean condition, Integer code, String key) {
        if (condition) {
            throw new BusinessExceptions(code, key);
        }
    }
}

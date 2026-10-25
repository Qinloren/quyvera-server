package com.zeeyeh.quyvera.auth.enums;

import com.zeeyeh.quyvera.common.core.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证错误定义
 * @author Qinloren
 * @date 2026/10/24 16:29
 */
@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    AUTH_NOT_SUPPORTED_STRATEGY_ERROR(32001, "error.auth.not.supported.strategy"),
    ;
    private final Integer code;
    private final String key;
}

package com.zeeyeh.quyvera.plugin.sdk.enums;

import com.zeeyeh.quyvera.common.core.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 插件错误定义
 * @author Qinloren
 * @date 2026/10/25 17:37
 */
@Getter
@AllArgsConstructor
public enum PluginErrorCode implements ErrorCode {
    AUTH_STRATEGY_TYPE_NOT_FOUND(30000, "策略类型不能为空"),
    AUTH_STRATEGY_EXISTS(30001, "策略已经存在"),
    AUTH_STRATEGY_NOT_EXISTS(30002, "策略不存在"),
    VERSION_NOT_LESS_THAN_ZERO(30003, "版本号不能小于0"),
    VERSION_BUILD_ILLEGAL(30004, "版本构建号不合法"),
    VERSION_ILLEGAL(30005, "版本号不合法"),
    ;
    private final Integer code;
    private final String key;
}

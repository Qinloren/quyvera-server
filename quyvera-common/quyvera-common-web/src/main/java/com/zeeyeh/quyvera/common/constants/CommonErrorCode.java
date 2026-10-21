package com.zeeyeh.quyvera.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zeeyeh.quyvera.common.core.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 公共错误码
 * @author Qinloren
 * @date 2026/10/20 15:04
 */
@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    SYSTEM_ERROR(10000, "error.system.error"),
    PARAM_INVALID(12000, "error.param.invalid"),
    NOT_AUTHORIZED(13000, "error.not.authorized"),
    NOT_LOGIN(14000, "error.not.login"),
    ;
    @JsonValue
    private final Integer code;
    private final String key;

    @Override
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static CommonErrorCode fromCode(Integer code) {
        return ErrorCode.fromCode(CommonErrorCode.class, code);
    }
}

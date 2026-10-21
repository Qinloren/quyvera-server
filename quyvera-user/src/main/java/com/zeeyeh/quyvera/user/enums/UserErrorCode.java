package com.zeeyeh.quyvera.user.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zeeyeh.quyvera.common.core.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Qinloren
 * @date 2026/10/20 15:53
 */
@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USERNAME_EXIST(31001, "error.username.exist"),
    EMAIL_EXIST(31002, "error.email.exist"),
    PHONE_EXIST(31003, "error.phone.exist"),
    USER_NOT_EXIST(31004, "error.user.not.exist"),
    USER_UPDATE_FAILED(31004, "error.user.update.failed"),
    USER_PASSWORD_ERROR(31005, "error.user.password.error"),
    ;
    @JsonValue
    private final Integer code;
    private final String key;

    @JsonCreator
    public static UserErrorCode fromCode(Integer code) {
        return ErrorCode.fromCode(UserErrorCode.class, code);
    }
}

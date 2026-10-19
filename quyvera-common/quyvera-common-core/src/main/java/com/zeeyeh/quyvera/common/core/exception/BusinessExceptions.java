package com.zeeyeh.quyvera.common.core.exception;

import cn.hutool.core.text.StrFormatter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Map;

/**
 * 业务异常
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessExceptions extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;

    private String detailMessage;

    public BusinessExceptions(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessExceptions(Integer code, String message, Object... args) {
        super(StrFormatter.format(message, args));
        this.code = code;
    }

    public BusinessExceptions(Integer code, String message, Map<String, Object> args) {
        super(StrFormatter.format(message, args, true));
        this.code = code;
    }
}


package com.zeeyeh.quyvera.plugin.sdk.exceptions;

/**
 * 插件异常
 * @author Qinloren
 * @date 2026/10/25 21:40
 */
public class InvalidPluginException extends RuntimeException {
    public InvalidPluginException(String message) {
        super(message);
    }

    public InvalidPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}

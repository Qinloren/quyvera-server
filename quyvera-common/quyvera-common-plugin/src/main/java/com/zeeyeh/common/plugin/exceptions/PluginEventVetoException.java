package com.zeeyeh.common.plugin.exceptions;

/**
 * 事件被取消异常
 * @author Qinloren
 * @date 2026/11/16 14:32
 */
public class PluginEventVetoException extends RuntimeException {
    public PluginEventVetoException(String message) {
        super(message);
    }

    public PluginEventVetoException(String message, Throwable cause) {
        super(message, cause);
    }
}

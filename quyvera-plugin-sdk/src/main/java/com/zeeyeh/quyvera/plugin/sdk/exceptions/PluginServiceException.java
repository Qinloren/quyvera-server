package com.zeeyeh.quyvera.plugin.sdk.exceptions;

/**
 * @author Qinloren
 * @date 2026/11/16 20:27
 */
public class PluginServiceException extends RuntimeException {
    public PluginServiceException(String message) {
        super(message);
    }
    public PluginServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

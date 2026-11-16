package com.zeeyeh.quyvera.plugin.service.plugin.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;

/**
 * @author Qinloren
 * @date 2026/11/16 20:28
 */
@Getter
@Setter
public class PluginApiDefinition {
    private final String api;
    private final Object bean;
    private final Method method;
    private Class<?>[] parameterTypes;
    private final String[] parameterNames;

    public PluginApiDefinition(String api, Object bean, Method method, String[] parameterNames) {
        this.api = api;
        this.bean = bean;
        this.method = method;
        this.parameterNames = parameterNames;
    }
}

package com.zeeyeh.quyvera.plugin.service.plugin.service;

import com.zeeyeh.quyvera.plugin.sdk.service.PluginApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Qinloren
 * @date 2026/11/16 20:30
 */
@Slf4j
public class PluginApiRegistry implements SmartInitializingSingleton {
    private final ApplicationContext applicationContext;
    private final Map<String, PluginApiDefinition> apis = new ConcurrentHashMap<>();

    public PluginApiRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        registerAll();
    }

    private void registerAll() {
        Map<String, Object> beans = applicationContext.getBeansOfType(Object.class);
        for (Object bean : beans.values()) {
            registerBean(bean);
        }
        log.info("插件服务 API 注册完成，共注册 {} 个 API", apis.size());
    }

    private void registerBean(Object bean) {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        ReflectionUtils.doWithMethods(targetClass, method -> {
            if (method.isBridge() || method.isSynthetic()) {
                return;
            }
            PluginApi annotation = AnnotatedElementUtils.findMergedAnnotation(method, PluginApi.class);
            if (annotation == null) {
                return;
            }
            register(bean, method, annotation);
        });
    }

    private void register(Object bean, Method targetMethod, PluginApi annotation) {
        String api = annotation.value().trim();
        if (api.isEmpty()) {
            throw new IllegalStateException("PluginApi名称不能为空: " + targetMethod);
        }
        if (!Modifier.isPublic(targetMethod.getModifiers()) || Modifier.isStatic(targetMethod.getModifiers())) {
            throw new IllegalStateException("PluginAPi 必须标注在 public 非 static 方法上: " + targetMethod);
        }
        String[] parameterNames = annotation.params();
        Class<?>[] parameterTypes = targetMethod.getParameterTypes();
        if (parameterNames.length != parameterTypes.length) {
            throw new IllegalStateException("PluginApi 参数名称数量与方法参数数量不一致, API: %s, 方法: %s".formatted(api, targetMethod));
        }
        for (String parameterName : parameterNames) {
            if (parameterName == null || parameterName.isBlank()) {
                throw new IllegalStateException("PluginApi 参数名称不能为空 API: %s".formatted(api));
            }
        }
        Method invocableMethod;
        try {
            Method specificMethod = AopUtils.getMostSpecificMethod(targetMethod, bean.getClass());
            invocableMethod = AopUtils.selectInvocableMethod(specificMethod, bean.getClass());
        } catch (IllegalStateException exception) {
            throw new IllegalStateException("无法通过 Spring Bean 调用 PluginApi: %s 。如果该 Bean 使用 JDK 动态代理，请确保该方法也暴露在代理接口上。", exception);
        }
        PluginApiDefinition definition = new PluginApiDefinition(api, bean, invocableMethod, parameterNames);
        PluginApiDefinition previous = apis.putIfAbsent(api, definition);
        if (previous != null) {
            throw new IllegalStateException("PluginApi 重复注册: " + api);
        }
        log.debug("注册插件服务 API: {}", api);
    }

    public PluginApiDefinition get(String api) {
        return apis.get(api);
    }

    public boolean exists(String api) {
        return apis.containsKey(api);
    }
}

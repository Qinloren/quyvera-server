package com.zeeyeh.quyvera.plugin.service.plugin.service;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeeyeh.quyvera.plugin.sdk.exceptions.PluginServiceException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Qinloren
 * @date 2026/11/16 20:48
 */
@Component
@RequiredArgsConstructor
public class PluginServiceDispatcher {

    private final PluginApiRegistry apiRegistry;

    /**
     * 调用宿主开放的 API。
     *
     * @param pluginId 插件标识
     * @param api      API 名称
     * @param params   调用参数
     * @return JSON 兼容的返回值
     */
    public Object invoke(
            String pluginId,
            String api,
            Map<String, Object> params
    ) {
        if (StringUtils.isBlank(pluginId)) {
            throw new PluginServiceException("插件Id不能为空");
        }

        if (StringUtils.isBlank(api)) {
            throw new PluginServiceException("API 名称不能为空");
        }

        // 查找 API 定义
        PluginApiDefinition definition = apiRegistry.get(api);

        if (definition == null) {
            throw new PluginServiceException(
                    "插件服务 API 不存在: " + api
            );
        }

        // 转换调用参数
        Object[] arguments = convertArguments(definition, params);

        Object result;

        try {
            Method method = definition.getMethod();

            // 通过 Spring Bean（保留代理对象）调用原始业务方法
            result = method.invoke(
                    definition.getBean(),
                    arguments
            );

        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getTargetException();

            throw new PluginServiceException(
                    "调用主程序 API 失败: %s, 原因: %s"
                            .formatted(api, cause.getMessage()),
                    cause
            );

        } catch (Exception exception) {
            throw new PluginServiceException(
                    "执行主程序 API 失败: " + api,
                    exception
            );
        }

        if (result == null
                || definition.getMethod().getReturnType() == void.class) {
            return null;
        }

        // 将宿主对象转换成 JSON 兼容的数据结构
        try {
            return JSON.parse(JSON.toJSONString(result));

        } catch (Exception exception) {
            throw new PluginServiceException(
                    "主程序 API 返回值无法转换为插件数据: " + api,
                    exception
            );
        }
    }

    /**
     * 转换 API 调用参数。
     */
    private Object[] convertArguments(
            PluginApiDefinition definition,
            Map<String, Object> params
    ) {
        Map<String, Object> actualParams =
                params == null ? Map.of() : params;

        String[] names = definition.getParameterNames();
        Class<?>[] types = definition.getParameterTypes();

        Set<String> allowedNames = new HashSet<>(
                Arrays.asList(names)
        );

        // 检查是否传入了不存在的参数
        for (String suppliedName : actualParams.keySet()) {
            if (!allowedNames.contains(suppliedName)) {
                throw new PluginServiceException(
                        "API %s 不存在参数: %s"
                                .formatted(
                                        definition.getApi(),
                                        suppliedName
                                )
                );
            }
        }

        Object[] arguments = new Object[names.length];

        for (int i = 0; i < names.length; i++) {
            String name = names[i];

            if (!actualParams.containsKey(name)) {
                throw new PluginServiceException(
                        "API %s 缺少参数: %s"
                                .formatted(
                                        definition.getApi(),
                                        name
                                )
                );
            }

            Object value = actualParams.get(name);

            // 基本类型不能接收 null
            if (value == null && types[i].isPrimitive()) {
                throw new PluginServiceException(
                        "API %s 的基本类型参数不能为 null: %s"
                                .formatted(
                                        definition.getApi(),
                                        name
                                )
                );
            }

            try {
                if (value == null) {
                    arguments[i] = null;
                } else {
                    String json = JSON.toJSONString(value);

                    arguments[i] = JSON.parseObject(
                            json,
                            types[i]
                    );
                }

            } catch (Exception exception) {
                throw new PluginServiceException(
                        "API %s 参数类型转换失败: %s"
                                .formatted(
                                        definition.getApi(),
                                        name
                                ),
                        exception
                );
            }
        }

        return arguments;
    }
}

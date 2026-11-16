package com.zeeyeh.quyvera.plugin.service.plugin.service;

import com.zeeyeh.quyvera.plugin.sdk.service.PluginServiceInvoker;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Qinloren
 * @date 2026/11/16 21:01
 */
@Component
@RequiredArgsConstructor
public class PluginServiceInvokerFactory {
    private final PluginServiceDispatcher dispatcher;

    public PluginServiceInvoker create(String pluginId) {
        if (StringUtils.isBlank(pluginId)) {
            throw new IllegalArgumentException("pluginId 不能为空");
        }
        return (api, params) -> dispatcher.invoke(pluginId, api, params);
    }
}

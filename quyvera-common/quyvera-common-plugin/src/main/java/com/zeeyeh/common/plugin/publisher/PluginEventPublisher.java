package com.zeeyeh.common.plugin.publisher;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;

/**
 * 插件事件发布器
 * @author Qinloren
 * @date 2026/11/16 14:43
 */
@FunctionalInterface
public interface PluginEventPublisher {

    /**
     * 同步发布事件
     * @param event 事件对象
     */
    void publishSync(Event event);
}

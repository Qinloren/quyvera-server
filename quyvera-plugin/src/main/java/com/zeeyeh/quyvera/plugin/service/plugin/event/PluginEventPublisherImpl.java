package com.zeeyeh.quyvera.plugin.service.plugin.event;

import com.zeeyeh.common.plugin.publisher.PluginEventPublisher;
import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.service.plugin.manager.EventManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 插件事件发布器实现
 * @author Qinloren
 * @date 2026/11/16 15:01
 */
@Component
@RequiredArgsConstructor
public class PluginEventPublisherImpl implements PluginEventPublisher {


    private final EventManager eventManager;

    @Override
    public void publishSync(Event event) {
        eventManager.callEventSync(event);
    }
}

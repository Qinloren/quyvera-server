package com.zeeyeh.quyvera.plugin.sdk.event;

/**
 * 插件事件注册接口
 * @author Qinloren
 * @date 2026/11/15 17:05
 */
public interface EventRegistry {

    /**
     * 注册事件监听器
     * @param eventType 事件类型
     * @param listener 监听器
     * @param <T> 事件类型
     */
    <T extends Event> void register(Class<T> eventType, Listener<T> listener);

    /**
     * 注销指定事件监听器。
     *
     * @param eventType 事件类型
     * @param listener 监听器
     * @param <T> 事件类型
     * @return 是否成功注销
     */
    <T extends Event> boolean unregister(Class<T> eventType, Listener<T> listener);

    /**
     * 触发事件。
     *
     * @param event 事件实例
     */
    void callEvent(Event event);
}

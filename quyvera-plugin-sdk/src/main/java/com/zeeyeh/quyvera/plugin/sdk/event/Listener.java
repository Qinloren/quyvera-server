package com.zeeyeh.quyvera.plugin.sdk.event;

/**
 * 事件监听器接口
 * @author Qinloren
 * @date 2026/10/27 20:48
 */
public interface Listener<T extends Event> {

    /**
     * 事件处理逻辑
     * @param event 事件体
     */
    void on(T event);

    /**
     * 数值越小越先执行
     * @return 排序编号
     */
    default int order() {
        return Orders.DEFAULT;
    }

    /**
     * 若事件已被取消，是否跳过本监听器
     * @return 是否跳过
     */
    default boolean skipOnVeto() {
        return false;
    }
}

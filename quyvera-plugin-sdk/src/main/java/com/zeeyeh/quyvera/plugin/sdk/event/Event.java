package com.zeeyeh.quyvera.plugin.sdk.event;

import lombok.Getter;

/**
 * 事件基类
 * @author Qinloren
 * @date 2026/10/27 20:23
 */
@Getter
public abstract class Event {
    private final boolean async;

    protected Event() {
        this(false);
    }

    public Event(boolean async) {
        this.async = async;
    }

}

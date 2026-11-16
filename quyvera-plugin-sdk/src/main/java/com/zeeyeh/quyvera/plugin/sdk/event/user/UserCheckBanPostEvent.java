package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;

/**
 * 校验用户是否封禁后事件
 * @author Qinloren
 * @date 2026/11/16 18:11
 */
public class UserCheckBanPostEvent extends Event {

    /**
     * 封禁状态
     */
    private boolean banned;

    /**
     * 是否封禁
     */
    public boolean isBan() {
        return banned;
    }
}

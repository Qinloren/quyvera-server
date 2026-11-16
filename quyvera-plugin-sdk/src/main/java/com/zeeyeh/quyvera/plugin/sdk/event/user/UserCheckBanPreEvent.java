package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;

/**
 * 校验用户是否封禁前事件
 * @author Qinloren
 * @date 2026/11/16 18:11
 */
public class UserCheckBanPreEvent extends Event implements Vetoable {
    private boolean vetoed;

    /**
     * 用户Id
     */
    private Long userId;

    @Override
    public boolean isVetoed() {
        return vetoed;
    }

    @Override
    public void setVetoed(boolean vetoed) {
        this.vetoed = vetoed;
    }
}

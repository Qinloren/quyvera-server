package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;

/**
 * 取消封禁用户前事件
 * @author Qinloren
 * @date 2026/11/16 17:55
 */
public class UserUnBanPreEvent extends Event implements Vetoable {
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

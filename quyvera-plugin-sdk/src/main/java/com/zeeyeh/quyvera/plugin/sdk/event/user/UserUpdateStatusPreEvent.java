package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import com.zeeyeh.quyvera.plugin.sdk.event.user.enums.UserStatus;

/**
 * 更新用户状态前事件
 * @author Qinloren
 * @date 2026/11/16 17:47
 */
public class UserUpdateStatusPreEvent extends Event implements Vetoable {
    private boolean vetoed;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 用户状态
     */
    private UserStatus status;

    @Override
    public boolean isVetoed() {
        return vetoed;
    }

    @Override
    public void setVetoed(boolean vetoed) {
        this.vetoed = vetoed;
    }
}

package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import lombok.Getter;
import lombok.Setter;

/**
 * 查询用户封禁详情前事件
 * @author Qinloren
 * @date 2026/11/16 18:00
 */
public class UserFindBanDetailPreEvent extends Event implements Vetoable {
    private boolean vetoed;

    /**
     * 用户Id
     */
    @Getter
    @Setter
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

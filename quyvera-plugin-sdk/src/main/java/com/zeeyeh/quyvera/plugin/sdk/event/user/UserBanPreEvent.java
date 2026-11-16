package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 封禁用户前事件
 * @author Qinloren
 * @date 2026/11/16 17:51
 */
public class UserBanPreEvent extends Event implements Vetoable {
    private boolean vetoed;

    /**
     * 用户Id
     */
    @Getter
    @Setter
    private Long userId;

    /**
     * 封禁原因
     */
    @Getter
    @Setter
    private String reason;

    /**
     * 开始时间
     */
    @Getter
    @Setter
    private LocalDateTime startTime;

    /**
     * 到期时间，NULL为永久
     */
    @Getter
    @Setter
    private LocalDateTime expireTime;

    @Override
    public boolean isVetoed() {
        return vetoed;
    }

    @Override
    public void setVetoed(boolean vetoed) {
        this.vetoed = vetoed;
    }
}

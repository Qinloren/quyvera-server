package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新用户密码前事件
 * @author Qinloren
 * @date 2026/11/16 17:43
 */
public class UserUpdatePasswordPreEvent extends Event implements Vetoable {
    private boolean vetoed;
    /**
     * 用户Id
     */
    @Getter
    @Setter
    private Long userId;

    /**
     * 旧密码
     */
    @Getter
    @Setter
    private String oldPassword;

    /**
     * 新密码
     */
    @Getter
    @Setter
    private String newPassword;

    @Override
    public boolean isVetoed() {
        return vetoed;
    }

    @Override
    public void setVetoed(boolean vetoed) {
        this.vetoed = vetoed;
    }
}

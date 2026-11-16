package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import com.zeeyeh.quyvera.plugin.sdk.event.user.enums.UserStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户更新前事件
 * @author Qinloren
 * @date 2026/11/16 17:23
 */
public class UserUpdatePreEvent extends Event implements Vetoable {
    private boolean vetoed;

    /**
     * 用户Id
     */
    @Getter
    @Setter
    private Long userId;

    /**
     * 用户名
     */
    @Getter
    @Setter
    private String username;

    /**
     * 密码
     */
    @Getter
    @Setter
    private String password;

    /**
     * 手机号
     */
    @Getter
    @Setter
    private String phone;

    /**
     * 邮箱
     */
    @Getter
    @Setter
    private String email;

    /**
     * 状态：0=正常、1=禁用、2=未激活
     */
    @Getter
    @Setter
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

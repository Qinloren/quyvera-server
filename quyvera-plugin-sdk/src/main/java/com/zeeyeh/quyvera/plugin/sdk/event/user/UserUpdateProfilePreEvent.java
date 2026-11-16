package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import com.zeeyeh.quyvera.plugin.sdk.event.user.enums.UserGender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 更新用户资料前事件
 * @author Qinloren
 * @date 2026/11/16 18:18
 */
public class UserUpdateProfilePreEvent extends Event implements Vetoable {
    private boolean vetoed;

    /**
     * 用户Id
     */
    @Getter
    @Setter
    private Long userId;

    /**
     * 昵称
     */
    @Getter
    @Setter
    private String nickname;

    /**
     * 性别：0=女、1=男、2=未知
     */
    @Getter
    @Setter
    private UserGender gender;

    /**
     * 生日
     */
    @Getter
    @Setter
    private LocalDate birthday;

    /**
     * 个人简介
     */
    @Getter
    @Setter
    private String bio;

    /**
     * 所在地
     */
    @Getter
    @Setter
    private String location;

    /**
     * 个性签名
     */
    @Getter
    @Setter
    private String signature;

    /**
     * 个人网站
     */
    @Getter
    @Setter
    private String website;

    /**
     * 头像地址
     */
    @Getter
    @Setter
    private String avatar;

    @Override
    public boolean isVetoed() {
        return vetoed;
    }

    @Override
    public void setVetoed(boolean vetoed) {
        this.vetoed = vetoed;
    }
}

package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.user.enums.UserGender;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 更新用户资料后事件
 * @author Qinloren
 * @date 2026/11/16 18:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserUpdateProfilePostEvent extends Event {

    /**
     * 资料Id
     */
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别：0=女、1=男、2=未知
     */
    private UserGender gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 所在地
     */
    private String location;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 个人网站
     */
    private String website;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

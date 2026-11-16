package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 查询用户后事件
 * @author Qinloren
 * @date 2026/11/16 17:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFindDetailPostEvent extends Event {

    /**
     * 用户Id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态：0=正常、1=禁用、2=未激活
     */
    private UserStatus status;

    /**
     * 注册IP
     */
    private String registerIp;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

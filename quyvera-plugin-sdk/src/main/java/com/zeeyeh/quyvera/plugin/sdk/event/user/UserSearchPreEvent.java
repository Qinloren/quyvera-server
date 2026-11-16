package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import com.zeeyeh.quyvera.plugin.sdk.event.user.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户搜索前事件
 * @author Qinloren
 * @date 2026/11/16 17:38
 */
public class UserSearchPreEvent extends Event implements Vetoable {
    private boolean vetoed;
    /**
     * 用户Id
     */
    @Getter
    @Setter
    private Long id;

    /**
     * 用户名
     */
    @Getter
    @Setter
    private String username;

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

    /**
     * 最后登录起始时间
     */
    @Getter
    @Setter
    private LocalDateTime lastLoginTimeStart;

    /**
     * 最后登录截止时间
     */
    @Getter
    @Setter
    private LocalDateTime lastLoginTimeEnd;

    /**
     * 创建起始时间
     */
    @Getter
    @Setter
    private LocalDateTime createTimeStart;

    /**
     * 创建截止时间
     */
    @Getter
    @Setter
    private LocalDateTime createTimeEnd;

    /**
     * 更新起始时间
     */
    @Getter
    @Setter
    private LocalDateTime updateTimeStart;

    /**
     * 更新截止时间
     */
    @Getter
    @Setter
    private LocalDateTime updateTimeEnd;

    /**
     * 排序字段
     */
    @Getter
    @Setter
    private String sort;

    /**
     * 当前页码
     */
    @Getter
    @Setter
    private Integer page;

    /**
     * 每页大小
     */
    @Getter
    @Setter
    private Integer size;

    @Override
    public boolean isVetoed() {
        return vetoed;
    }

    @Override
    public void setVetoed(boolean vetoed) {
        this.vetoed = vetoed;
    }
}

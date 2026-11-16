package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import com.zeeyeh.quyvera.plugin.sdk.event.user.enums.UserBanStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 搜索用户封禁列表前事件
 * @author Qinloren
 * @date 2026/11/16 18:05
 */
public class UserSearchBanPreEvent extends Event implements Vetoable {
    private boolean vetoed;

    /**
     * 封禁Id
     */
    @Getter
    @Setter
    private Long id;

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
     * 操作者Id
     */
    @Getter
    @Setter
    private Long operatorId;

    /**
     * 开始时间
     */
    @Getter
    @Setter
    private LocalDateTime startTimeStart;

    /**
     * 开始时间
     */
    @Getter
    @Setter
    private LocalDateTime startTimeEnd;

    /**
     * 到期时间，expireTimeStart与expireTimeEnd均为NULL时则表示永久
     */
    @Getter
    @Setter
    private LocalDateTime expireTimeStart;

    /**
     * 到期时间，expireTimeStart与expireTimeEnd均为NULL时则表示永久
     */
    @Getter
    @Setter
    private LocalDateTime expireTimeEnd;

    /**
     * 状态：0=封禁中、1=已解封
     */
    @Getter
    @Setter
    private UserBanStatus status;

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

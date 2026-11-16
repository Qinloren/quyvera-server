package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.user.enums.UserBanStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 取消封禁用户后事件
 * @author Qinloren
 * @date 2026/11/16 17:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserUnBanPostEvent extends Event {

    /**
     * 封禁Id
     */
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 操作者Id
     */
    private Long operatorId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 到期时间，NULL为永久
     */
    private LocalDateTime expireTime;

    /**
     * 状态：0=封禁中、1=已解封
     */
    private UserBanStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

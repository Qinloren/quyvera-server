package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 校验用户资料是否存在后事件
 * @author Qinloren
 * @date 2026/11/16 18:27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserHasProfilePostEvent extends Event {
    /**
     * 是否存在
     */
    private boolean exists;
}

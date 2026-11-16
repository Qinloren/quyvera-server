package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新用户密码后事件
 * @author Qinloren
 * @date 2026/11/16 17:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserUpdatePasswordPostEvent extends Event {
}

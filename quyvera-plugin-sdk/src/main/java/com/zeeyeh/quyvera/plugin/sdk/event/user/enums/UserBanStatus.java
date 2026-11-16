package com.zeeyeh.quyvera.plugin.sdk.event.user.enums;

import com.zeeyeh.quyvera.common.core.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户封禁状态
 * @author Qinloren
 * @date 2026/11/16 17:53
 */
@Getter
@RequiredArgsConstructor
public enum UserBanStatus implements BaseEnum<Integer> {
    BLOCKED(0, "封禁中"),
    UNBLOCKED(1, "已解封"),
    ;
    private final Integer value;
    private final String description;
}

package com.zeeyeh.quyvera.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.zeeyeh.quyvera.common.core.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Qinloren
 * @date 2026/10/23 17:42
 */
@Getter
@RequiredArgsConstructor
public enum UserBanStatus implements BaseEnum<Integer> {
    BLOCKED(0, "封禁中"),
    UNBLOCKED(1, "已解封"),
    ;
    @EnumValue
    private final Integer value;
    private final String description;
}

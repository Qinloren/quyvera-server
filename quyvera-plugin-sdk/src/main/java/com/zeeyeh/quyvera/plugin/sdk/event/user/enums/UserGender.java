package com.zeeyeh.quyvera.plugin.sdk.event.user.enums;

import com.zeeyeh.quyvera.common.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户性别枚举
 * @author Qinloren
 * @date 2026/11/16 18:14
 */
@Getter
@AllArgsConstructor
public enum UserGender implements BaseEnum<Integer> {
    FEMALE(0, "女"),
    MALE(1, "男"),
    UNKNOWN(2, "未知"),
    ;
    private final Integer value;
    private final String description;
}

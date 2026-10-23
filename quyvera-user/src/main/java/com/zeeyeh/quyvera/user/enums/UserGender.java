package com.zeeyeh.quyvera.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.zeeyeh.quyvera.common.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Qinloren
 * @date 2026/10/23 19:01
 */
@Getter
@AllArgsConstructor
public enum UserGender implements BaseEnum<Integer> {
    FEMALE(0, "女"),
    MALE(1, "男"),
    UNKNOWN(2, "未知"),
    ;
    @EnumValue
    private final Integer value;
    private final String description;
}

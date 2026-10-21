package com.zeeyeh.quyvera.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.zeeyeh.quyvera.common.core.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户状态枚举
 * @author Qinloren
 * @date 2026/10/19 23:27
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus implements BaseEnum<Integer> {
    NORMAL(0, "正常"),
    DISABLE(1, "禁用"),
    UNAUTHORIZED(2, "未验证"),
    ;
    @EnumValue
    private final Integer value;
    private final String description;
}

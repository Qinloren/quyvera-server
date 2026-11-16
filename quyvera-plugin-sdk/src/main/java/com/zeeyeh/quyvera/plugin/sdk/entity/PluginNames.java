package com.zeeyeh.quyvera.plugin.sdk.entity;

import com.zeeyeh.quyvera.plugin.sdk.exceptions.InvalidPluginException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * 插件名校验工具
 * 规则：首字符必须为英文字母，其余仅允许英文字母、数字、下划线，总长度 1~{@value #MAX_LENGTH}。
 * @author Qinloren
 * @date 2026/11/8 14:05
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginNames {
    public static final int MAX_LENGTH = 64;


    public static final String RULE = "1~" + MAX_LENGTH + " 个字符，首字符必须为英文字母，其余仅允许英文字母、数字、下划线";

    private static final Pattern PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");

    /**
     * 判断插件名是否合法
     * @param name 插件名
     * @return 合法返回 true
     */
    public static boolean isValid(String name) {
        if (name == null || name.isEmpty() || name.length() > MAX_LENGTH) {
            return false;
        }
        return PATTERN.matcher(name).matches();
    }

    /**
     * 校验并返回合法插件名，非法时直接抛异常
     * @param name 原始插件名
     * @return 合法插件名（与入参相同）
     * @throws InvalidPluginException 插件名为空或不满足命名规则
     */
    public static String require(String name) {
        if (!isValid(name)) {
            throw new InvalidPluginException("[插件系统] 非法插件名: " + name + "，要求：" + RULE);
        }
        return name;
    }
}

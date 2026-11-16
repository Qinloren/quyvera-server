package com.zeeyeh.common.plugin.annotations;

import java.lang.annotation.*;

/**
 * 插件事件重复注解容器
 * @author Qinloren
 * @date 2026/11/16 16:23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginEvents {

    PluginEvent[] value();
}

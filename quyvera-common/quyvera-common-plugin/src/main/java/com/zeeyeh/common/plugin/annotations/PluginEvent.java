package com.zeeyeh.common.plugin.annotations;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;

import java.lang.annotation.*;

/**
 * 插件事件触发注解
 * @author Qinloren
 * @date 2026/11/16 14:26
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(PluginEvents.class)
public @interface PluginEvent {
    /**
     * 事件类型
     */
    Class<? extends Event> value();

    /**
     * 事件执行阶段
     */
    Phase phase() default Phase.PRE;

    /**
     * 参与事件映射的方法参数
     */
    PluginParam[] params() default {};

    /**
     * 返回值对应事件体中的属性名称
     * 若返回值为一个实体类则不生效
     * @return 属性名称
     */
    String returnProperty() default "";

    /**
     * 事件触发时机
     */
    enum Phase {
        /**
         * 业务执行前
         */
        PRE,

        /**
         * 业务执行后
         */
        POST
    }
}

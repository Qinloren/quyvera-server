package com.zeeyeh.quyvera.plugin.sdk.service;

import java.lang.annotation.*;

/**
 * 对插件开放Api注解标记
 * @author Qinloren
 * @date 2026/11/16 20:21
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginApi {

    /**
     * 插件调用时使用的API名称
     * @return API名称
     */
    String value();

    /**
     * 参数名称。顺序必须与方法参数顺序一致
     */
    String[] params() default {};
}

package com.zeeyeh.common.plugin.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Qinloren
 * @date 2026/11/16 15:45
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginParam {

  /**
   * 方法参数名称
   */
  String name();

  /**
   * 参数映射模式
   */
  Mode mode();

  /**
   * 参数映射模式
   */
  enum Mode {
    PROPERTY,
    BEAN,
  }
}

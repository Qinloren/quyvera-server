package com.zeeyeh.quyvera.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 跳过响应统一结果包装
 * @author Qinloren
 * @date 2026/10/23 20:28
 */
@Target({
        ElementType.METHOD,
        ElementType.TYPE,
})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipResultWrapper {
}

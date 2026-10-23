package com.zeeyeh.quyvera.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 跳过统一结果过敏装饰
 * @author Qinloren
 * @date 2026/10/23 20:29
 */
@Target({
        ElementType.METHOD,
        ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipResultDecorate {
}

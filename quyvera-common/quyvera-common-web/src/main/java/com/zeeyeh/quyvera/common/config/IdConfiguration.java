package com.zeeyeh.quyvera.common.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Id配置类
 * @author Qinloren
 * @date 2026/10/20 16:29
 */
@Configuration
public class IdConfiguration {

    @Bean
    public Snowflake snowflake() {
        return IdUtil.getSnowflake();
    }
}

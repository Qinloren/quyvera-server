package com.zeeyeh.common.plugin.config;

import com.zeeyeh.common.plugin.aspect.PluginEventAspect;
import com.zeeyeh.common.plugin.publisher.PluginEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

/**
 * 插件事件自动配置
 * @author Qinloren
 * @date 2026/11/16 14:58
 */
@Configuration(proxyBeanMethods = false)
@EnableAspectJAutoProxy
public class PluginEventAutoConfiguration {

    @Bean
    @ConditionalOnBean(PluginEventPublisher.class)
    public PluginEventAspect pluginEventAspect(PluginEventPublisher eventPublisher) {
        return new PluginEventAspect(eventPublisher);
    }
}

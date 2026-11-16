package com.zeeyeh.quyvera.plugin.service.plugin.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Qinloren
 * @date 2026/11/16 21:03
 */
@Configuration
public class PluginServiceConfiguration {

    @Bean
    public PluginApiRegistry pluginApiRegistry(ApplicationContext applicationContext) {
        return new PluginApiRegistry(applicationContext);
    }
}

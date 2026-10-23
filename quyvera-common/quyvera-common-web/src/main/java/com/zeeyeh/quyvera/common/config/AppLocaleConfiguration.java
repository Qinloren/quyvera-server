package com.zeeyeh.quyvera.common.config;

import com.zeeyeh.quyvera.common.config.properties.AppLocaleProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author Qinloren
 * @date 2026/10/20 20:53
 */
@AutoConfiguration
@EnableConfigurationProperties(AppLocaleProperties.class)
public class AppLocaleConfiguration {
}

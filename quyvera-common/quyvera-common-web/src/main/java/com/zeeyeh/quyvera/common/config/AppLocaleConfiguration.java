package com.zeeyeh.quyvera.common.config;

import com.zeeyeh.quyvera.common.config.properties.AppLocaleProperties;
import com.zeeyeh.quyvera.common.factory.LocaleFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;

/**
 * @author Qinloren
 * @date 2026/10/20 20:53
 */
@AutoConfiguration
@EnableConfigurationProperties(AppLocaleProperties.class)
public class AppLocaleConfiguration {
}

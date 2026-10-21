package com.zeeyeh.quyvera.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用国际化配置
 * @author Qinloren
 * @date 2026/10/20 20:51
 */
@Data
@ConfigurationProperties(prefix = "app.locale")
public class AppLocaleProperties {

    /**
     * 国际化文件目录路径
     */
    private String dir = "./locales";
}

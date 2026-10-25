package com.zeeyeh.quyvera.plugin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 插件服务主类
 * @author Qinloren
 * @date 2026/10/25 22:06
 */
@SpringBootApplication(scanBasePackages = {
        "com.zeeyeh.quyvera.common",
        "com.zeeyeh.quyvera.plugin"
})
public class PluginApplication {
    public static void main(String[] args) {
        SpringApplication.run(PluginApplication.class, args);
    }
}

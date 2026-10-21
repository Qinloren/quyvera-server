package com.zeeyeh.quyvera.gateway.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关层安全配置属性
 * @author Qinloren
 * @date 2026/10/20 19:53
 */
@Data
@ConfigurationProperties(prefix = "app.gateway")
public class GatewaySecurityProperties {
    private List<String> whitelist = new ArrayList<>();
}

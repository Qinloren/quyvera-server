package com.zeeyeh.quyvera.gateway;

import com.zeeyeh.quyvera.gateway.config.properties.GatewaySecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
        scanBasePackages = {
                "com.zeeyeh.quyvera.gateway",
                "com.zeeyeh.quyvera.common",
        }
)
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

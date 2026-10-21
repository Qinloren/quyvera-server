package com.zeeyeh.quyvera.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用安全配置属性
 * @author Qinloren
 * @date 2026/10/20 17:49
 */
@Data
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    /**
     * 是否启用安全配置
     */
    private boolean enabled = false;

    /**
     * 白名单列表，用于排除不需要安全验证的URL路径
     */
    private List<String> whitelist = new ArrayList<>();

    /**
     * 用户ID头名称
     */
    private String userIdHeader = "X-User-Id";

    /**
     * 角色头名称
     */
    private String rolesHeader = "X-User-Roles";
}

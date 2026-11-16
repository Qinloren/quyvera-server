package com.zeeyeh.quyvera.plugin.sdk.manager;

import com.zeeyeh.quyvera.common.auth.AuthStrategy;
import com.zeeyeh.quyvera.common.core.utils.BusinessAsserts;
import com.zeeyeh.quyvera.plugin.sdk.enums.PluginErrorCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证策略管理器
 * @author Qinloren
 * @date 2026/10/25 17:35
 */
public class AuthStrategyManager {

    private final Map<String, AuthStrategy> strategies = new ConcurrentHashMap<>();

    /**
     * 注册策略
     * @param type 认证策略类型
     * @param authStrategy 认证策略
     */
    public void register(String type, AuthStrategy authStrategy) {
        BusinessAsserts.check(StringUtils.isBlank(type), PluginErrorCode.AUTH_STRATEGY_TYPE_NOT_FOUND);
        AuthStrategy previous = strategies.putIfAbsent(type, authStrategy);
        BusinessAsserts.check(previous != null && previous != authStrategy, PluginErrorCode.AUTH_STRATEGY_EXISTS);
    }

    /**
     * 获取策略
     * @param type 认证策略类型
     * @return 策略
     */
    public AuthStrategy get(String type) {
        AuthStrategy authStrategy = strategies.get(type);
        BusinessAsserts.check(authStrategy == null, PluginErrorCode.AUTH_STRATEGY_NOT_EXISTS);
        return authStrategy;
    }

    /**
     * 获取策略
     * @param type 认证策略类型
     * @param fallback 默认策略
     * @return 策略
     */
    public AuthStrategy getOrDefault(String type, AuthStrategy fallback) {
        return strategies.getOrDefault(type, fallback);
    }

    /**
     * 注销策略
     * @param type 认证策略类型
     */
    public void unregister(String type) {
        strategies.remove(type);
    }

    /**
     * 校验策略是否注册
     * @param type 认证策略类型
     * @return 是否注册
     */
    public boolean isRegister(String type) {
        return strategies.containsKey(type);
    }

    /**
     * 获取已注册的策略集合
     * @return 认证策略集合
     */
    public Collection<AuthStrategy> allStrategies() {
        return List.copyOf(strategies.values());
    }
}

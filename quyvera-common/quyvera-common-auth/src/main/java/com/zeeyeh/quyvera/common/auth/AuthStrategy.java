package com.zeeyeh.quyvera.common.auth;

/**
 * 登录策略接口
 * @author Qinloren
 * @date 2026/10/24 15:37
 */
public interface AuthStrategy {

    /**
     * 获取认证策略类型
     * @return 认证策略类型
     */
    String type();

    /**
     * 认证处理
     * @param request 认证请求
     * @return 认证结果
     */
    AuthResult authenticate(AuthRequest request);
}

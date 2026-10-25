package com.zeeyeh.quyvera.common.auth;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 认证结果
 * @author Qinloren
 * @date 2026/10/24 15:37
 */
@Data
public abstract class AuthResult {

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 访问令牌过期时间（秒）
     */
    private Long accessExpireTime;
}

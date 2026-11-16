package com.zeeyeh.quyvera.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * token对
 * @author Qinloren
 * @date 2026/10/24 14:44
 */
@Data
@AllArgsConstructor
public class TokenPair {

    /**
     * access token
     */
    private String accessToken;

    /**
     * refresh token
     */
    private String refreshToken;

    /**
     * access token过期时间（秒）
     */
    private Long accessExpiresIn;
}

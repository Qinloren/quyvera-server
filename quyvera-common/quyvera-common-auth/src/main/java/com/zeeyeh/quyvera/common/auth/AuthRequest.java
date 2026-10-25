package com.zeeyeh.quyvera.common.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 登录请求
 * @author Qinloren
 * @date 2026/10/24 15:32
 */
@Data
@AllArgsConstructor
public class AuthRequest {

    /**
     * 登录方式
     */
    private String type;

    /**
     * 登录所需参数
     */
    private Map<String, Object> params;
}

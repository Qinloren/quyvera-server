package com.zeeyeh.quyvera.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录会话
 * @author Qinloren
 * @date 2026/10/24 14:39
 */
@Data
@AllArgsConstructor
public class LoginSession {

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 设备Id
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 客户端类型
     */
    private String clientType;

    /**
     * IP地址
     */
    private String ip;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 刷新令牌Hash
     */
    private Long refreshHash;

    /**
     * 创建时间
     */
    private Long createTime;
}

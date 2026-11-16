package com.zeeyeh.quyvera.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录请求参数
 * @author Qinloren
 * @date 2026/10/23 23:46
 */
@Data
@AllArgsConstructor
public class LoginDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 客户端Id
     */
    private String clientId;

    /**
     * 客户端类型
     */
    private String clientType;

    /**
     * 设备Id
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;
}

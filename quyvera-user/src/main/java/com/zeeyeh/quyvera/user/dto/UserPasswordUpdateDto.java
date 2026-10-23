package com.zeeyeh.quyvera.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Qinloren
 * @date 2026/10/23 21:07
 */
@Data
@AllArgsConstructor
public class UserPasswordUpdateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}

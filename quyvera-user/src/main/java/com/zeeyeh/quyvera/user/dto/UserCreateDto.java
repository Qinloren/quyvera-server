package com.zeeyeh.quyvera.user.dto;

import com.zeeyeh.quyvera.user.domain.User;
import com.zeeyeh.quyvera.user.enums.UserStatus;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户创建请求参数
 * @author Qinloren
 * @date 2026/10/19 23:44
 */
@Data
@AllArgsConstructor
@AutoMapper(target = User.class)
public class UserCreateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态：0=正常、1=禁用、2=未激活
     */
    private UserStatus status;
}

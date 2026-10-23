package com.zeeyeh.quyvera.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户封禁更新请求参数
 * @author Qinloren
 * @date 2026/10/23 17:51
 */
@Data
@AllArgsConstructor
public class UserUpdateBanDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 到期时间，NULL为永久
     */
    private LocalDateTime expireTime;
}

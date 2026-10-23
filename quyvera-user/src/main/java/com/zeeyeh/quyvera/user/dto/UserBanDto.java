package com.zeeyeh.quyvera.user.dto;

import com.zeeyeh.quyvera.user.domain.UserBan;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户封禁请求参数
 * @author Qinloren
 * @date 2026/10/23 17:48
 */
@Data
@AllArgsConstructor
@AutoMapper(target = UserBan.class)
public class UserBanDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 到期时间，NULL为永久
     */
    private LocalDateTime expireTime;
}

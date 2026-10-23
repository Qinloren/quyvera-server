package com.zeeyeh.quyvera.user.vo;

import com.zeeyeh.quyvera.user.enums.UserBanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户封禁视图对象
 * @author Qinloren
 * @date 2026/10/23 17:39
 */
@Data
@AllArgsConstructor
public class UserBanVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 封禁Id
     */
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 操作者Id
     */
    private Long operatorId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 到期时间，NULL为永久
     */
    private LocalDateTime expireTime;

    /**
     * 状态：0=封禁中、1=已解封
     */
    private UserBanStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

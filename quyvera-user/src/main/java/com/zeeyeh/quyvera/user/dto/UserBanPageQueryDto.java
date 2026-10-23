package com.zeeyeh.quyvera.user.dto;

import com.zeeyeh.quyvera.user.enums.UserBanStatus;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户封禁分页查询请求参数
 * @author Qinloren
 * @date 2026/10/23 17:59
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserBanPageQueryDto implements Serializable {

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
    private LocalDateTime startTimeStart;

    /**
     * 开始时间
     */
    private LocalDateTime startTimeEnd;

    /**
     * 到期时间，expireTimeStart与expireTimeEnd均为NULL时则表示永久
     */
    private LocalDateTime expireTimeStart;

    /**
     * 到期时间，expireTimeStart与expireTimeEnd均为NULL时则表示永久
     */
    private LocalDateTime expireTimeEnd;

    /**
     * 状态：0=封禁中、1=已解封
     */
    private UserBanStatus status;

    /**
     * 排序字段
     */
    @Builder.Default
    private String sort = "createTime";

    /**
     * 当前页码
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 10;
}

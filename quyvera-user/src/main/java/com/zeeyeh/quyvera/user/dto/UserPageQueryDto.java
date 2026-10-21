package com.zeeyeh.quyvera.user.dto;

import com.zeeyeh.quyvera.user.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户分页查询请求参数
 * @author Qinloren
 * @date 2026/10/20 00:08
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserPageQueryDto {

    /**
     * 用户Id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

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

    /**
     * 最后登录起始时间
     */
    private LocalDateTime lastLoginTimeStart;

    /**
     * 最后登录截止时间
     */
    private LocalDateTime lastLoginTimeEnd;

    /**
     * 创建起始时间
     */
    private LocalDateTime createTimeStart;

    /**
     * 创建截止时间
     */
    private LocalDateTime createTimeEnd;

    /**
     * 更新起始时间
     */
    private LocalDateTime updateTimeStart;

    /**
     * 更新截止时间
     */
    private LocalDateTime updateTimeEnd;

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

package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import com.zeeyeh.quyvera.plugin.sdk.event.user.enums.UserBanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索用户封禁列表后事件
 * @author Qinloren
 * @date 2026/11/16 18:05
 */
public class UserSearchBanPostEvent extends Event {

    /**
     * 结果列表
     */
    private List<UserBanVo> results;

    /**
     * 当前页码
     */
    private long number;

    /**
     * 每页记录数
     */
    private long size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 是否是第一页
     */
    private boolean first;

    /**
     * 是否是最后一页
     */
    private boolean last;

    /**
     * 是否拥有上一页
     */
    private boolean previous;

    /**
     * 是否拥有下一页
     */
    private boolean next;

    @Data
    @AllArgsConstructor
    public static class UserBanVo {

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

}

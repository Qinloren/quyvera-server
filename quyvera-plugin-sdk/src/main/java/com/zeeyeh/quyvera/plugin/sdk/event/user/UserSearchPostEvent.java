package com.zeeyeh.quyvera.plugin.sdk.event.user;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户搜索后事件
 * @author Qinloren
 * @date 2026/11/16 17:41
 */
public class UserSearchPostEvent extends Event {

    /**
     * 结果列表
     */
    private List<UserVo> results;

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
    public static class UserVo implements Serializable {

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
         * 注册IP
         */
        private String registerIp;

        /**
         * 最后登录时间
         */
        private LocalDateTime lastLoginTime;

        /**
         * 最后登录IP
         */
        private String lastLoginIp;

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

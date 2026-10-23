package com.zeeyeh.quyvera.user.vo;

import com.zeeyeh.quyvera.user.enums.UserGender;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户资料视图对象
 * @author Qinloren
 * @date 2026/10/23 18:49
 */
@Data
@AllArgsConstructor
public class UserProfileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 资料Id
     */
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别：0=女、1=男、2=未知
     */
    private UserGender gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 所在地
     */
    private String location;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 个人网站
     */
    private String website;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

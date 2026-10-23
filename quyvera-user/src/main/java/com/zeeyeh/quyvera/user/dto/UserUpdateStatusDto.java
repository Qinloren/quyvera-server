package com.zeeyeh.quyvera.user.dto;

import com.zeeyeh.quyvera.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户更新状态请求参数
 * @author Qinloren
 * @date 2026/10/23 21:15
 */
@Data
@AllArgsConstructor
public class UserUpdateStatusDto {
    private UserStatus status;
}

package com.zeeyeh.quyvera.user.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.zeeyeh.quyvera.common.core.entity.PageResult;
import com.zeeyeh.quyvera.user.domain.User;
import com.zeeyeh.quyvera.user.dto.UserCreateDto;
import com.zeeyeh.quyvera.user.dto.UserPageQueryDto;
import com.zeeyeh.quyvera.user.dto.UserUpdateDto;
import com.zeeyeh.quyvera.user.enums.UserStatus;
import com.zeeyeh.quyvera.user.vo.UserVo;

import java.util.Collection;
import java.util.List;

/**
 * 用户表业务接口
 * @author Qinloren
 */
public interface UserService extends IService<User> {

    /**
     * 创建用户
     * @param dto 用户创建请求参数
     * @return 用户信息
     */
    UserVo createUser(UserCreateDto dto);

    /**
     * 更新用户信息
     * @param dto 用户更新请求参数
     * @return 用户信息
     */
    UserVo updateUser(Long userId, UserUpdateDto dto);

    /**
     * 获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    UserVo getUserByUsername(String username);

    /**
     * 获取用户信息
     * @param phone 手机号
     * @return 用户信息
     */
    UserVo getUserByPhone(String phone);

    /**
     * 获取用户信息
     * @param email 邮箱地址
     * @return 用户信息
     */
    UserVo getUserByEmail(String email);

    /**
     * 批量获取用户信息
     * @param userIds 用户Id集合
     * @return 用户信息列表
     */
    List<UserVo> listUserByIds(Collection<Long> userIds);

    /**
     * 分页查询用户信息
     * @param dto 分页查询请求参数
     * @return 分页查询结果
     */
    PageResult<UserVo> pageQuery(UserPageQueryDto dto);

    /**
     * 验证用户密码是否匹配
     * @param userId 用户Id
     * @param password 密码
     * @return 是否匹配
     */
    boolean verifyPassword(Long userId, String password);

    /**
     * 更新用户密码
     * @param userId 用户Id
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新用户最后登录信息
     * @param userId 用户Id
     * @param loginIp 登录IP地址
     */
    void updateLastLogin(Long userId, String loginIp);

    /**
     * 更新用户状态
     * @param userId 用户Id
     * @param status 用户状态
     */
    void updateStatus(Long userId, UserStatus status);

    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return 是否可用
     */
    boolean isUsernameAvailable(String username);

    /**
     * 检查手机号是否可用
     * @param phone 手机号
     * @return 是否可用
     */
    boolean isPhoneAvailable(String phone);

    /**
     * 检查邮箱是否可用
     * @param email 邮箱地址
     * @return 是否可用
     */
    boolean isEmailAvailable(String email);
}

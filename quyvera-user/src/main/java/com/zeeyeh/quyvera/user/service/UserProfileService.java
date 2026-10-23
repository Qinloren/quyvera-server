package com.zeeyeh.quyvera.user.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.zeeyeh.quyvera.user.domain.UserProfile;
import com.zeeyeh.quyvera.user.dto.UserProfileCreateDto;
import com.zeeyeh.quyvera.user.dto.UserProfileUpdateDto;
import com.zeeyeh.quyvera.user.vo.UserProfileVo;

import java.util.Collection;
import java.util.List;

/**
 * 用户资料表业务接口
 * @author Qinloren
 */
public interface UserProfileService extends IService<UserProfile> {

    /**
     * 创建用户资料
     * @param dto 用户资料请求参数
     */
    void createProfile(UserProfileCreateDto dto);

    /**
     * 更新用户资料视图对象
     * @param userId 用户Id
     * @param dto 用户资料更新请求参数
     * @return 用户资料信息
     */
    UserProfileVo updateProfile(Long userId, UserProfileUpdateDto dto);

    /**
     * 获取用户资料
     * @param userId 用户Id
     * @return 用户资料信息
     */
    UserProfileVo getProfileByUserId(Long userId);

    /**
     * 获取用户资料列表
     * @param userIds 用户Id列表
     * @return 用户资料信息列表
     */
    List<UserProfileVo> listProfileByUserIds(Collection<Long> userIds);

    /**
     * 判断用户是否已经存在资料
     * @param userId 用户Id
     * @return 是否已经存在资料
     */
    boolean hasProfile(Long userId);
}

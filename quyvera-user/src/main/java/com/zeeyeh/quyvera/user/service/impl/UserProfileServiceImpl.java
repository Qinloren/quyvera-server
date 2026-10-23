package com.zeeyeh.quyvera.user.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zeeyeh.quyvera.common.core.utils.BusinessAsserts;
import com.zeeyeh.quyvera.user.convert.UserProfileConvert;
import com.zeeyeh.quyvera.user.domain.UserProfile;
import com.zeeyeh.quyvera.user.dto.UserProfileCreateDto;
import com.zeeyeh.quyvera.user.dto.UserProfileUpdateDto;
import com.zeeyeh.quyvera.user.enums.UserErrorCode;
import com.zeeyeh.quyvera.user.service.UserProfileService;
import com.zeeyeh.quyvera.user.mapper.UserProfileMapper;
import com.zeeyeh.quyvera.user.vo.UserProfileVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 用户资料表业务接口实现类
 * @author Qinloren
 */
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    private final UserProfileConvert userProfileConvert;

    public UserProfileServiceImpl(UserProfileConvert userProfileConvert) {
        this.userProfileConvert = userProfileConvert;
    }

    @Override
    public void createProfile(UserProfileCreateDto dto) {
        UserProfile profile = this.lambdaQuery()
                .eq(UserProfile::getUserId, dto.getUserId())
                .one();
        BusinessAsserts.check(profile != null, UserErrorCode.USER_PROFILE_EXIST);
        UserProfile userProfile = userProfileConvert.toEntity(dto);
        userProfile.setCreateTime(LocalDateTime.now());
        userProfile.setUpdateTime(LocalDateTime.now());
        userProfile.setDeleted(Boolean.FALSE);
        this.save(userProfile);
    }

    @Override
    public UserProfileVo updateProfile(Long userId, UserProfileUpdateDto dto) {
        UserProfile profile = this.lambdaQuery()
                .eq(UserProfile::getUserId, userId)
                .one();
        BusinessAsserts.check(profile != null, UserErrorCode.USER_PROFILE_NOT_EXIST);
        boolean updated = this.lambdaUpdate()
                .set(StringUtils.isNotBlank(dto.getNickname()), UserProfile::getNickname, dto.getNickname())
                .set(dto.getGender() != null, UserProfile::getGender, dto.getGender())
                .set(dto.getBirthday() != null, UserProfile::getBirthday, dto.getBirthday())
                .set(StringUtils.isNotBlank(dto.getBio()), UserProfile::getBio, dto.getBio())
                .set(StringUtils.isNotBlank(dto.getLocation()), UserProfile::getBio, dto.getBio())
                .set(StringUtils.isNotBlank(dto.getSignature()), UserProfile::getBio, dto.getBio())
                .set(StringUtils.isNotBlank(dto.getWebsite()), UserProfile::getBio, dto.getBio())
                .set(StringUtils.isNotBlank(dto.getAvatar()), UserProfile::getBio, dto.getBio())
                .eq(UserProfile::getUserId, userId)
                .update();
        BusinessAsserts.check(!updated, UserErrorCode.USER_PROFILE_UPDATE_FAILED);
        profile = this.lambdaQuery()
                .eq(UserProfile::getUserId, userId)
                .one();
        return userProfileConvert.toVo(profile);
    }

    @Override
    public UserProfileVo getProfileByUserId(Long userId) {
        UserProfile profile = this.lambdaQuery()
                .eq(UserProfile::getUserId, userId)
                .one();
        BusinessAsserts.check(profile == null, UserErrorCode.USER_PROFILE_NOT_EXIST);
        return userProfileConvert.toVo(profile);
    }

    @Override
    public List<UserProfileVo> listProfileByUserIds(Collection<Long> userIds) {
        if (userIds == null) {
            return Collections.emptyList();
        }
        List<UserProfile> list = this.lambdaQuery()
                .in(UserProfile::getUserId, userIds)
                .list();
        return userProfileConvert.toVoList(list);
    }

    @Override
    public boolean hasProfile(Long userId) {
        return this.lambdaQuery()
                .eq(UserProfile::getUserId, userId)
                .exists();
    }
}





package com.zeeyeh.quyvera.user.convert;

import com.zeeyeh.quyvera.user.domain.UserProfile;
import com.zeeyeh.quyvera.user.dto.UserProfileCreateDto;
import com.zeeyeh.quyvera.user.vo.UserProfileVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * 用户资料转换器接口
 * @author Qinloren
 * @date 2026/10/23 19:21
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserProfileConvert {

    /**
     * 将用户资料创建请求参数转换为实体
     * @param dto 用户资料创建请求参数
     * @return 用户资料信息
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    UserProfile toEntity(UserProfileCreateDto dto);

    /**
     * 将用户资料实体转换为用户资料实体
     * @param userProfile 用户资料实体
     * @return 用户资料信息
     */
    UserProfileVo toVo(UserProfile userProfile);

    /**
     * 将用户资料实体转换为用户资料信息列表
     * @param userProfiles 用户资料实体列表
     * @return 用户资料信息列表
     */
    List<UserProfileVo> toVoList(List<UserProfile> userProfiles);
}

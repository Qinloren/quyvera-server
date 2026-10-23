package com.zeeyeh.quyvera.user.convert;

import com.zeeyeh.quyvera.user.domain.UserBan;
import com.zeeyeh.quyvera.user.dto.UserBanDto;
import com.zeeyeh.quyvera.user.vo.UserBanVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 用户封禁转换器接口
 * @author Qinloren
 * @date 2026/10/23 18:07
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserBanConvert {

    /**
     * 将用户封禁请求参数转换为用户封禁实体
     * @param dto 用户封禁请求参数
     * @return 用户封禁实体
     */
    UserBan toEntity(UserBanDto dto);

    /**
     * 将用户封禁转换为用户封禁视图
     * @param userBan 用户封禁实体
     * @return 用户封禁视图
     */
    UserBanVo toVo(UserBan userBan);

    /**
     * 将用户封禁列表转换为用户封禁视图列表
     * @param userBans 用户封禁列表
     * @return 用户封禁视图列表
     */
    List<UserBanVo> toVoList(List<UserBan> userBans);
}

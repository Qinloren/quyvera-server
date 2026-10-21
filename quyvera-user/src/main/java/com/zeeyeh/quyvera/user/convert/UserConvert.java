package com.zeeyeh.quyvera.user.convert;

import com.zeeyeh.quyvera.user.domain.User;
import com.zeeyeh.quyvera.user.dto.UserCreateDto;
import com.zeeyeh.quyvera.user.vo.UserVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 用户转换器接口
 * @author Qinloren
 * @date 2026/10/20 16:00
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserConvert {

    /**
     * 将用户创建请求参数对象转换为实体
     * @param dto 用户创建请求参数对象
     * @return 实体对象
     */
    User toEntity(UserCreateDto dto);

    /**
     * 将实体对象转换为用户视图对象
     * @param user 实体对象
     * @return 用户视图对象
     */
    UserVo toVo(User user);

    /**
     * 将实体对象转换为用户视图对象
     * @param users 实体对象列表
     * @return 用户视图对象
     */
    List<UserVo> toVoList(List<User> users);
}

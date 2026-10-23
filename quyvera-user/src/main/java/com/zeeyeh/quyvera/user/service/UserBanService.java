package com.zeeyeh.quyvera.user.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.zeeyeh.quyvera.common.core.entity.PageResult;
import com.zeeyeh.quyvera.user.domain.UserBan;
import com.zeeyeh.quyvera.user.dto.UserBanDto;
import com.zeeyeh.quyvera.user.dto.UserBanPageQueryDto;
import com.zeeyeh.quyvera.user.vo.UserBanVo;
import com.zeeyeh.quyvera.user.vo.UserVo;

import java.util.Collection;
import java.util.List;

/**
 * 用户封禁记录表的业务接口
 * @author Qinloren
 */
public interface UserBanService extends IService<UserBan> {

    /**
     * 封禁用户
     * @param dto 用户封禁请求参数
     * @return 用户封禁信息
     */
    UserBanVo banUser(UserBanDto dto);

    /**
     * 解封用户
     * @param userId 用户Id
     * @return 用户封禁信息
     */
    UserBanVo unbanUser(Long userId);

    /**
     * 获取用户封禁信息
     * @param userId 用户Id
     * @return 用户封禁信息
     */
    UserBanVo getUserBanInfo(Long userId);

    /**
     * 批量获取用户封禁信息
     * @param userIds 用户Id集合
     * @return 用户封禁信息集合
     */
    List<UserBanVo> listUserBanInfoByIds(Collection<Long> userIds);

    /**
     * 分页查询用户封禁信息
     * @param dto 分页查询参数
     * @return 分页查询结果
     */
    PageResult<UserBanVo> pageQuery(UserBanPageQueryDto dto);

    /**
     * 检查用户是否被封禁
     * @param userId 用户Id
     * @return 是否被封禁
     */
    boolean checkUserBan(Long userId);
}

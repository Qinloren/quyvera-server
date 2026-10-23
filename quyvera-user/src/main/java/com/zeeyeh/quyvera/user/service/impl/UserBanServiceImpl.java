package com.zeeyeh.quyvera.user.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zeeyeh.quyvera.common.core.entity.PageResult;
import com.zeeyeh.quyvera.common.core.exception.BusinessExceptions;
import com.zeeyeh.quyvera.common.core.utils.BusinessAsserts;
import com.zeeyeh.quyvera.user.convert.UserBanConvert;
import com.zeeyeh.quyvera.user.domain.UserBan;
import com.zeeyeh.quyvera.user.dto.UserBanDto;
import com.zeeyeh.quyvera.user.dto.UserBanPageQueryDto;
import com.zeeyeh.quyvera.user.enums.UserBanStatus;
import com.zeeyeh.quyvera.user.enums.UserErrorCode;
import com.zeeyeh.quyvera.user.service.UserBanService;
import com.zeeyeh.quyvera.user.mapper.UserBanMapper;
import com.zeeyeh.quyvera.user.vo.UserBanVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 用户封禁记录表的业务实现类
 * @author Qinloren
 */
@Service
public class UserBanServiceImpl extends ServiceImpl<UserBanMapper, UserBan> implements UserBanService {

    private final UserBanConvert userBanConvert;
    private final Snowflake snowflake;

    public UserBanServiceImpl(UserBanConvert userBanConvert, Snowflake snowflake) {
        this.userBanConvert = userBanConvert;
        this.snowflake = snowflake;
    }

    @Override
    public UserBanVo banUser(UserBanDto dto) {
        BusinessAsserts.check(this.getById(dto.getUserId()) != null, UserErrorCode.USER_HAS_BANDED);
        UserBan userBan = userBanConvert.toEntity(dto);
        userBan.setId(snowflake.nextId());
        userBan.setCreateTime(LocalDateTime.now());
        userBan.setUpdateTime(LocalDateTime.now());
        this.save(userBan);
        return userBanConvert.toVo(userBan);
    }

    @Override
    public UserBanVo unbanUser(Long userId) {
        UserBan userBan = this.getById(userId);
        BusinessAsserts.check(userBan == null, UserErrorCode.USER_HAS_NOT_BANDED);
        boolean updated = this.lambdaUpdate()
                .set(UserBan::getStatus, UserBanStatus.UNBLOCKED)
                .set(UserBan::getUpdateTime, LocalDateTime.now())
                .eq(UserBan::getUserId, userId)
                .update();
        BusinessAsserts.check(!updated, UserErrorCode.USER_BAN_FAILED);
        userBan = this.getById(userId);
        return userBanConvert.toVo(userBan);
    }

    @Override
    public UserBanVo getUserBanInfo(Long userId) {
        UserBan userBan = this.getById(userId);
        BusinessAsserts.check(userBan == null, UserErrorCode.USER_HAS_NOT_BANDED);
        return userBanConvert.toVo(userBan);
    }

    @Override
    public List<UserBanVo> listUserBanInfoByIds(Collection<Long> userIds) {
        List<UserBan> list = this.lambdaQuery()
                .in(UserBan::getUserId, userIds)
                .list();
        return userBanConvert.toVoList(list);
    }

    @Override
    public PageResult<UserBanVo> pageQuery(UserBanPageQueryDto dto) {
        int pageNumber = Math.max(0, dto.getPage() - 1);
        Page<UserBan> page = Page.of(pageNumber, dto.getSize());
        page.addOrder(OrderItem.asc(dto.getSort()));
        LambdaQueryChainWrapper<UserBan> queryChainWrapper = this.lambdaQuery()
                .eq(dto.getId() != null, UserBan::getId, dto.getId())
                .eq(dto.getUserId() != null, UserBan::getUserId, dto.getUserId())
                .like(StringUtils.isNotBlank(dto.getReason()), UserBan::getReason, dto.getReason())
                .eq(dto.getOperatorId() != null, UserBan::getOperatorId, dto.getOperatorId())
                .ge(dto.getStartTimeStart() != null, UserBan::getStartTime, dto.getStartTimeStart())
                .le(dto.getStartTimeEnd() != null, UserBan::getStartTime, dto.getStartTimeEnd())
                .eq(dto.getStatus() != null, UserBan::getStatus, dto.getStatus());
        if (dto.getExpireTimeStart() != null && dto.getExpireTimeEnd() != null) {
            queryChainWrapper
                    .ge(UserBan::getExpireTime, dto.getExpireTimeStart())
                    .le(UserBan::getExpireTime, dto.getExpireTimeEnd());
        } else {
            queryChainWrapper.eq(UserBan::getExpireTime, null);
        }
        Page<UserBan> userBanPage = queryChainWrapper.page(page);
        List<UserBan> list = userBanPage.getRecords();
        List<UserBanVo> result = userBanConvert.toVoList(list);
        return new PageResult<>(
                result,
                userBanPage.getCurrent(),
                userBanPage.getSize(),
                userBanPage.getTotal(),
                userBanPage.getPages(),
                userBanPage.getCurrent() == 0,
                userBanPage.getCurrent() == userBanPage.getTotal(),
                userBanPage.hasPrevious(),
                userBanPage.hasNext()
        );
    }

    @Override
    public boolean checkUserBan(Long userId) {
        UserBan userBan = this.lambdaQuery()
                .eq(UserBan::getUserId, userId)
                .one();
        if (userBan == null) {
            return false;
        }
        return userBan.getStatus() == UserBanStatus.BLOCKED;
    }
}





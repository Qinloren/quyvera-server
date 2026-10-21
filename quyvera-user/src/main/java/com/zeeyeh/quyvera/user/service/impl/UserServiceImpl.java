package com.zeeyeh.quyvera.user.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zeeyeh.quyvera.common.core.entity.PageResult;
import com.zeeyeh.quyvera.common.core.exception.BusinessExceptions;
import com.zeeyeh.quyvera.common.core.utils.BusinessAsserts;
import com.zeeyeh.quyvera.user.convert.UserConvert;
import com.zeeyeh.quyvera.user.domain.User;
import com.zeeyeh.quyvera.user.dto.UserCreateDto;
import com.zeeyeh.quyvera.user.dto.UserPageQueryDto;
import com.zeeyeh.quyvera.user.dto.UserUpdateDto;
import com.zeeyeh.quyvera.user.enums.UserErrorCode;
import com.zeeyeh.quyvera.user.enums.UserStatus;
import com.zeeyeh.quyvera.user.mapper.UserMapper;
import com.zeeyeh.quyvera.user.service.UserService;
import com.zeeyeh.quyvera.user.vo.UserVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 用户表业务接口实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserConvert userConvert;
    private final Snowflake snowflake;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserConvert userConvert, Snowflake snowflake, PasswordEncoder passwordEncoder) {
        this.userConvert = userConvert;
        this.snowflake = snowflake;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserVo createUser(UserCreateDto dto) {
        BusinessAsserts.check(!this.isUsernameAvailable(dto.getUsername()), UserErrorCode.USERNAME_EXIST);
        BusinessAsserts.check(!this.isEmailAvailable(dto.getEmail()), UserErrorCode.EMAIL_EXIST);
        BusinessAsserts.check(!this.isPhoneAvailable(dto.getPhone()), UserErrorCode.PHONE_EXIST);
        User user = userConvert.toEntity(dto);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setId(snowflake.nextId());
        this.save(user);
        return userConvert.toVo(user);
    }

    @Override
    public UserVo updateUser(Long userId, UserUpdateDto dto) {
        User user = this.getById(userId);
        BusinessAsserts.check(user == null, UserErrorCode.USER_NOT_EXIST);
        boolean updated = this.lambdaUpdate()
                .set(StringUtils.isNotBlank(dto.getUsername()), User::getUsername, dto.getUsername())
                .set(StringUtils.isNotBlank(dto.getEmail()), User::getEmail, dto.getEmail())
                .set(StringUtils.isNotBlank(dto.getPhone()), User::getPhone, dto.getPhone())
                .set(dto.getStatus() != null, User::getStatus, dto.getStatus())
                .eq(User::getId, userId)
                .update();
        if (!updated) {
            throw new BusinessExceptions(UserErrorCode.USER_UPDATE_FAILED);
        }
        user = this.getById(userId);
        return userConvert.toVo(user);
    }

    @Override
    public UserVo getUserByUsername(String username) {
        User user = this.lambdaQuery()
                .eq(User::getUsername, username)
                .one();
        BusinessAsserts.check(user == null, UserErrorCode.USER_NOT_EXIST);
        return userConvert.toVo(user);
    }

    @Override
    public UserVo getUserByPhone(String phone) {
        User user = this.lambdaQuery()
                .eq(User::getPhone, phone)
                .one();
        BusinessAsserts.check(user == null, UserErrorCode.USER_NOT_EXIST);
        return userConvert.toVo(user);
    }

    @Override
    public UserVo getUserByEmail(String email) {
        User user = this.lambdaQuery()
                .eq(User::getEmail, email)
                .one();
        BusinessAsserts.check(user == null, UserErrorCode.USER_NOT_EXIST);
        return userConvert.toVo(user);
    }

    @Override
    public List<UserVo> listUserByIds(Collection<Long> userIds) {
        return userConvert.toVoList(this.listByIds(userIds));
    }

    @Override
    public PageResult<UserVo> pageQuery(UserPageQueryDto dto) {
        int pageNumber = Math.max(0, dto.getPage() - 1);
        Page<User> page = Page.of(pageNumber, dto.getSize());
        page.addOrder(OrderItem.asc(dto.getSort()));
        Page<User> userPage = this.lambdaQuery()
                .eq(dto.getId() != null, User::getId, dto.getId())
                .like(StringUtils.isNotBlank(dto.getUsername()), User::getUsername, dto.getUsername())
                .like(StringUtils.isNotBlank(dto.getPhone()), User::getPhone, dto.getPhone())
                .like(StringUtils.isNotBlank(dto.getEmail()), User::getEmail, dto.getEmail())
                .like(StringUtils.isNotBlank(dto.getPhone()), User::getPhone, dto.getPhone())
                .eq(dto.getStatus() != null, User::getStatus, dto.getStatus())
                .ge(dto.getLastLoginTimeStart() != null, User::getLastLoginTime, dto.getLastLoginTimeStart())
                .le(dto.getLastLoginTimeEnd() != null, User::getLastLoginTime, dto.getLastLoginTimeEnd())
                .ge(dto.getCreateTimeStart() != null, User::getCreateTime, dto.getCreateTimeStart())
                .le(dto.getCreateTimeEnd() != null, User::getCreateTime, dto.getCreateTimeEnd())
                .ge(dto.getUpdateTimeStart() != null, User::getUpdateTime, dto.getUpdateTimeStart())
                .le(dto.getUpdateTimeEnd() != null, User::getUpdateTime, dto.getUpdateTimeEnd())
                .page(page);
        List<User> list = userPage.getRecords();
        List<UserVo> result = userConvert.toVoList(list);
        return new PageResult<>(
                result,
                userPage.getCurrent(),
                userPage.getSize(),
                userPage.getTotal(),
                userPage.getPages(),
                userPage.getCurrent() == 0,
                userPage.getCurrent() == userPage.getTotal(),
                userPage.hasPrevious(),
                userPage.hasNext()
        );
    }

    @Override
    public boolean verifyPassword(Long userId, String password) {
        User user = this.getById(userId);
        BusinessAsserts.check(user == null, UserErrorCode.USER_NOT_EXIST);
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        BusinessAsserts.check(user == null, UserErrorCode.USER_NOT_EXIST);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessExceptions(UserErrorCode.USER_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    public void updateLastLogin(Long userId, String loginIp) {
        BusinessAsserts.check(this.getById(userId) == null, UserErrorCode.USER_NOT_EXIST);
        boolean updated = this.lambdaUpdate()
                .set(User::getLastLoginTime, LocalDateTime.now())
                .set(User::getLastLoginIp, loginIp)
                .eq(User::getId, userId)
                .update();
        if (updated) {
            throw new BusinessExceptions(UserErrorCode.USER_UPDATE_FAILED);
        }
    }

    @Override
    public void updateStatus(Long userId, UserStatus status) {
        BusinessAsserts.check(this.getById(userId) == null, UserErrorCode.USER_NOT_EXIST);
        boolean updated = this.lambdaUpdate()
                .set(User::getStatus, status)
                .eq(User::getId, userId)
                .update();
        if (updated) {
            throw new BusinessExceptions(UserErrorCode.USER_UPDATE_FAILED);
        }
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !this.lambdaQuery()
                .eq(User::getUsername, username)
                .exists();
    }

    @Override
    public boolean isPhoneAvailable(String phone) {
        return !this.lambdaQuery()
                .eq(User::getPhone, phone)
                .exists();
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !this.lambdaQuery()
                .eq(User::getEmail, email)
                .exists();
    }
}





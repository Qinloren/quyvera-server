package com.zeeyeh.quyvera.user.controller;

import com.zeeyeh.quyvera.common.core.entity.PageResult;
import com.zeeyeh.quyvera.user.dto.*;
import com.zeeyeh.quyvera.user.service.UserBanService;
import com.zeeyeh.quyvera.user.service.UserProfileService;
import com.zeeyeh.quyvera.user.service.UserService;
import com.zeeyeh.quyvera.user.vo.UserBanVo;
import com.zeeyeh.quyvera.user.vo.UserProfileVo;
import com.zeeyeh.quyvera.user.vo.UserVo;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserBanService userBanService;
    private final UserProfileService userProfileService;

    public UserController(UserService userService, UserBanService userBanService, UserProfileService userProfileService) {
        this.userService = userService;
        this.userBanService = userBanService;
        this.userProfileService = userProfileService;
    }

    /**
     * 创建用户
     * @param dto 用户信息
     * @return 用户信息
     */
    @PostMapping("/create")
    @ResponseBody
    public UserVo create(@RequestBody UserCreateDto dto) {
        return userService.createUser(dto);
    }

    /**
     * 更新用户
     * @param dto 用户信息
     * @return 用户信息
     */
    @PostMapping("/update/{uid}")
    @ResponseBody
    public UserVo update(@PathVariable("uid") Long userId, @RequestBody UserUpdateDto dto) {
        return userService.updateUser(userId, dto);
    }

    /**
     * 获取用户详情
     * @return 用户信息
     */
    @GetMapping("/detail/{uid}")
    @ResponseBody
    public UserVo detail(@PathVariable("uid") Long userId) {
        return userService.getUserById(userId);
    }

    /**
     * 搜索用户
     * @param dto 查询条件
     * @return 用户信息
     */
    @GetMapping("/search")
    @ResponseBody
    public PageResult<UserVo> search(UserPageQueryDto dto) {
        return userService.pageQuery(dto);
    }

    /**
     * 更新用户密码
     * @param userId 用户Id
     * @param dto 密码信息
     */
    @PostMapping("/update/password/{uid}")
    @ResponseBody
    public void updatePassword(@PathVariable("uid") Long userId, UserPasswordUpdateDto dto) {
        userService.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
    }

    /**
     * 更新用户状态
     * @param userId 用户Id
     * @param dto 状态信息
     */
    @PostMapping("/update/status/{uid}")
    @ResponseBody
    public void updateStatus(@PathVariable("uid") Long userId, @RequestBody UserUpdateStatusDto dto) {
        userService.updateStatus(userId, dto.getStatus());
    }

    /**
     * 封禁用户
     * @param dto 用户信息
     * @return 用户信息
     */
    @PostMapping("/ban")
    @ResponseBody
    public UserBanVo banUser(@RequestBody UserBanDto dto) {
        return userBanService.banUser(dto);
    }

    /**
     * 解封用户
     * @param uid 用户Id
     * @return 用户信息
     */
    @PostMapping("/unban/{uid}")
    @ResponseBody
    public UserBanVo unbanUser(@PathVariable Long uid) {
        return userBanService.unbanUser(uid);
    }

    /**
     * 获取用户封禁详情
     * @param userId 用户Id
     * @return 用户封禁详情
     */
    @GetMapping("/ban/detail/{uid}")
    @ResponseBody
    public UserBanVo detailBanUser(@PathVariable("uid")  Long userId) {
        return userBanService.getUserBanInfo(userId);
    }

    /**
     * 获取用户封禁列表
     * @param dto 查询条件
     * @return 用户封禁列表
     */
    @GetMapping("/ban/search")
    @ResponseBody
    public PageResult<UserBanVo> searchBan(UserBanPageQueryDto dto) {
        return userBanService.pageQuery(dto);
    }

    /**
     * 校验用户是否被封禁
     * @param userId 用户Id
     * @return 是否被封禁
     */
    @GetMapping("/ban/check/{uid}")
    @ResponseBody
    public boolean checkUserBan(@PathVariable("uid") Long userId) {
        return userBanService.checkUserBan(userId);
    }

    /**
     * 更新用户资料
     * @param dto 用户资料更新请求参数
     * @return 用户资料信息
     */
    @PostMapping("/update/profile/{uid}")
    @ResponseBody
    public UserProfileVo updateProfile(@PathVariable("uid") Long userId, @RequestBody UserProfileUpdateDto dto) {
        return userProfileService.updateProfile(userId, dto);
    }

    /**
     * 获取用户资料
     * @param userId 用户Id
     * @return 用户资料信息
     */
    @GetMapping("/profile/detail/{uid}")
    @ResponseBody
    public UserProfileVo detailProfile(@PathVariable("uid") Long userId) {
        return userProfileService.getProfileByUserId(userId);
    }

    /**
     * 检查用户资料是否存在
     * @param userId 用户Id
     * @return 是否存在
     */
    @GetMapping("/profile/exists/{uid}")
    @ResponseBody
    public boolean hasProfile(@PathVariable("uid") Long userId) {
        return userProfileService.hasProfile(userId);
    }
}

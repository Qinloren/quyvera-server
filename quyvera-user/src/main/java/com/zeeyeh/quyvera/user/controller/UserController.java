package com.zeeyeh.quyvera.user.controller;

import com.zeeyeh.common.plugin.annotations.PluginEvent;
import com.zeeyeh.common.plugin.annotations.PluginParam;
import com.zeeyeh.quyvera.common.core.entity.PageResult;
import com.zeeyeh.quyvera.plugin.sdk.event.user.*;
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
    @PluginEvent(UserCreatePreEvent.class)
    @PluginEvent(value = UserCreatePostEvent.class, phase = PluginEvent.Phase.POST)
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
    @PluginEvent(
            value = UserUpdatePreEvent.class,
            params = {
                    @PluginParam(name = "userId", mode = PluginParam.Mode.PROPERTY),
                    @PluginParam(name = "dto", mode = PluginParam.Mode.BEAN)
            }
    )
    @PluginEvent(value = UserUpdatePostEvent.class, phase = PluginEvent.Phase.POST)
    public UserVo update(@PathVariable("uid") Long userId, @RequestBody UserUpdateDto dto) {
        return userService.updateUser(userId, dto);
    }

    /**
     * 获取用户详情
     * @return 用户信息
     */
    @GetMapping("/detail/{uid}")
    @ResponseBody
    @PluginEvent(UserFindDetailPreEvent.class)
    @PluginEvent(value = UserFindDetailPostEvent.class, phase = PluginEvent.Phase.POST)
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
    @PluginEvent(UserSearchPreEvent.class)
    @PluginEvent(value = UserSearchPostEvent.class, phase = PluginEvent.Phase.POST)
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
    @PluginEvent(UserUpdatePasswordPreEvent.class)
    @PluginEvent(value = UserUpdatePasswordPostEvent.class, phase =  PluginEvent.Phase.POST)
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
    @PluginEvent(UserUpdateStatusPreEvent.class)
    @PluginEvent(value = UserUpdateStatusPostEvent.class, phase = PluginEvent.Phase.POST)
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
    @PluginEvent(UserBanPreEvent.class)
    @PluginEvent(value = UserBanPostEvent.class, phase = PluginEvent.Phase.POST)
    public UserBanVo banUser(@RequestBody UserBanDto dto) {
        return userBanService.banUser(dto);
    }

    /**
     * 解封用户
     * @param userId 用户Id
     * @return 用户信息
     */
    @PostMapping("/unban/{uid}")
    @ResponseBody
    @PluginEvent(UserUnBanPreEvent.class)
    @PluginEvent(value = UserUnBanPostEvent.class, phase = PluginEvent.Phase.POST)
    public UserBanVo unbanUser(@PathVariable("uid") Long userId) {
        return userBanService.unbanUser(userId);
    }

    /**
     * 获取用户封禁详情
     * @param userId 用户Id
     * @return 用户封禁详情
     */
    @GetMapping("/ban/detail/{uid}")
    @ResponseBody
    @PluginEvent(UserFindBanDetailPreEvent.class)
    @PluginEvent(value = UserFindBanDetailPostEvent.class, phase = PluginEvent.Phase.POST)
    public UserBanVo detailBanUser(@PathVariable("uid") Long userId) {
        return userBanService.getUserBanInfo(userId);
    }

    /**
     * 获取用户封禁列表
     * @param dto 查询条件
     * @return 用户封禁列表
     */
    @GetMapping("/ban/search")
    @ResponseBody
    @PluginEvent(UserSearchBanPreEvent.class)
    @PluginEvent(value = UserSearchBanPostEvent.class, phase = PluginEvent.Phase.POST)
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
    @PluginEvent(UserCheckBanPreEvent.class)
    @PluginEvent(value = UserCheckBanPostEvent.class, phase = PluginEvent.Phase.POST, returnProperty = "banned")
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
    @PluginEvent(
            value = UserUpdateProfilePreEvent.class,
            params = {
                    @PluginParam(name = "userId", mode = PluginParam.Mode.PROPERTY),
                    @PluginParam(name = "dto", mode = PluginParam.Mode.BEAN)
            }
    )
    @PluginEvent(value = UserUpdateProfilePostEvent.class, phase = PluginEvent.Phase.POST)
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
    @PluginEvent(UserFindProfileDetailPreEvent.class)
    @PluginEvent(value = UserFindProfileDetailPostEvent.class, phase = PluginEvent.Phase.POST)
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
    @PluginEvent(UserHasProfilePreEvent.class)
    @PluginEvent(value = UserHasProfilePostEvent.class, phase = PluginEvent.Phase.POST)
    public boolean hasProfile(@PathVariable("uid") Long userId) {
        return userProfileService.hasProfile(userId);
    }
}

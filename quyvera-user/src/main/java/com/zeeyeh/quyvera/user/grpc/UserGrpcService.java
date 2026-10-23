package com.zeeyeh.quyvera.user.grpc;

import com.zeeyeh.quyvera.user.api.v1.*;
import com.zeeyeh.quyvera.user.service.UserBanService;
import com.zeeyeh.quyvera.user.service.UserProfileService;
import com.zeeyeh.quyvera.user.service.UserService;
import com.zeeyeh.quyvera.user.vo.UserBanVo;
import com.zeeyeh.quyvera.user.vo.UserProfileVo;
import com.zeeyeh.quyvera.user.vo.UserVo;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.ZoneId;
import java.util.List;

/**
 * @author Qinloren
 * @date 2026/10/21 20:49
 */
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;
    private final UserBanService userBanService;
    private final UserProfileService userProfileService;

    public UserGrpcService(UserService userService, UserBanService userBanService, UserProfileService userProfileService) {
        super();
        this.userService = userService;
        this.userBanService = userBanService;
        this.userProfileService = userProfileService;
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserInfo> responseObserver) {
        UserVo userVo = userService.getUserById(request.getUserId());
        responseObserver.onNext(toUserInfo(userVo));
        responseObserver.onCompleted();
    }

    @Override
    public void batchGetUserByIds(BatchGetUsersByIdsRequest request, StreamObserver<BatchGetUserByIdsResponse> responseObserver) {
        List<UserVo> vos = userService.listUserByIds(request.getUserIdsList());
        List<UserInfo> list = vos.stream()
                .map(this::toUserInfo)
                .toList();
        responseObserver.onNext(BatchGetUserByIdsResponse.newBuilder()
                .addAllUsers(list)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserByUsername(GetUserByUsernameRequest request, StreamObserver<UserInfo> responseObserver) {
        UserVo userVo = userService.getUserByUsername(request.getUsername());
        responseObserver.onNext(toUserInfo(userVo));
        responseObserver.onCompleted();
    }

    @Override
    public void getUserByPhone(GetUserByPhoneRequest request, StreamObserver<UserInfo> responseObserver) {
        UserVo userVo = userService.getUserByPhone(request.getPhone());
        responseObserver.onNext(toUserInfo(userVo));
        responseObserver.onCompleted();
    }

    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserInfo> responseObserver) {
        UserVo userVo = userService.getUserByEmail(request.getEmail());
        responseObserver.onNext(toUserInfo(userVo));
        responseObserver.onCompleted();
    }

    @Override
    public void verifyPassword(VerifyPasswordRequest request, StreamObserver<VerifyPasswordResponse> responseObserver) {
        boolean verified = userService.verifyPassword(request.getUserId(), request.getPassword());
        responseObserver.onNext(VerifyPasswordResponse.newBuilder()
                .setMatched(verified)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateLastLogin(UpdateLastLoginRequest request, StreamObserver<UpdateLastLoginResponse> responseObserver) {
        userService.updateLastLogin(request.getUserId(), request.getLoginIp());
        responseObserver.onNext(UpdateLastLoginResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void batchGetUserBanByIds(BatchGetUserBanByIdsRequest request, StreamObserver<BatchGetUserBanByIdsResponse> responseObserver) {
        List<UserBanVo> list = userBanService.listUserBanInfoByIds(request.getUserIdsList());
        List<UserBanInfo> result = list.stream()
                .map(this::toUserBanInfo)
                .toList();
        responseObserver.onNext(BatchGetUserBanByIdsResponse.newBuilder()
                .addAllUserBans(result)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void batchGetUserProfileByIds(BatchGetUserProfileByIdsRequest request, StreamObserver<BatchGetUserProfileByIdsResponse> responseObserver) {
        List<UserProfileVo> list = userProfileService.listProfileByUserIds(request.getUserIdsList());
        List<UserProfileInfo> result = list.stream()
                .map(this::toUserProfileInfo)
                .toList();
        responseObserver.onNext(BatchGetUserProfileByIdsResponse.newBuilder()
                .addAllUserProfiles(result)
                .build());
        responseObserver.onCompleted();
    }

    private UserInfo toUserInfo(UserVo userVo) {
        return UserInfo.newBuilder()
                .setUserId(userVo.getId())
                .setUsername(userVo.getUsername())
                .setPhone(userVo.getPhone())
                .setEmail(userVo.getEmail())
                .setStatus(toUserStatus(userVo.getStatus()))
                .setRegisterIp(userVo.getRegisterIp())
                .setLastLoginTime(userVo.getLastLoginTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .setLastLoginIp(userVo.getLastLoginIp())
                .setCreateTime(userVo.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .setUpdateTime(userVo.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
    }

    private UserBanInfo toUserBanInfo(UserBanVo userBanVo) {
        return UserBanInfo.newBuilder()
                .setId(userBanVo.getId())
                .setUserId(userBanVo.getUserId())
                .setReason(userBanVo.getReason())
                .setOperatorId(userBanVo.getOperatorId())
                .setStartTime(userBanVo.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .setExpireTime(userBanVo.getExpireTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .setStatus(toUserBanStatus(userBanVo.getStatus()))
                .setCreateTime(userBanVo.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .setUpdateTime(userBanVo.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
    }

    private UserProfileInfo toUserProfileInfo(UserProfileVo userProfileVo) {
        return UserProfileInfo.newBuilder()
                .setId(userProfileVo.getId())
                .setUserId(userProfileVo.getUserId())
                .setNickname(userProfileVo.getNickname())
                .setGender(toUserGender(userProfileVo.getGender()))
                .setBirthday(userProfileVo.getBirthday().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .setBio(userProfileVo.getBio())
                .setLocation(userProfileVo.getLocation())
                .setSignature(userProfileVo.getSignature())
                .setWebsite(userProfileVo.getWebsite())
                .setAvatar(userProfileVo.getAvatar())
                .setCreateTime(userProfileVo.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .setUpdateTime(userProfileVo.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
    }

    private UserGender toUserGender(com.zeeyeh.quyvera.user.enums.UserGender gender) {
        if (gender == null) {
            return null;
        }
        return UserGender.forNumber(gender.ordinal());
    }

    private UserBanStatus toUserBanStatus(com.zeeyeh.quyvera.user.enums.UserBanStatus status) {
        if (status == null) {
            return null;
        }
        return UserBanStatus.forNumber(status.ordinal());
    }

    private UserStatus toUserStatus(com.zeeyeh.quyvera.user.enums.UserStatus status) {
        if (status == null) {
            return null;
        }
        return UserStatus.forNumber(status.ordinal());
    }
}

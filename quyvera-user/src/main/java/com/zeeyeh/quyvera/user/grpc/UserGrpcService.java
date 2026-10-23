package com.zeeyeh.quyvera.user.grpc;

import com.zeeyeh.quyvera.user.api.v1.*;
import com.zeeyeh.quyvera.user.domain.User;
import com.zeeyeh.quyvera.user.service.UserService;
import com.zeeyeh.quyvera.user.vo.UserVo;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Qinloren
 * @date 2026/10/21 20:49
 */
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    public UserGrpcService(UserService userService) {
        super();
        this.userService = userService;
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

    private UserInfo toUserInfo(UserVo userVo) {
        return UserInfo.newBuilder()
                .setUserId(userVo.getId())
                .setUsername(userVo.getUsername())
                .setPhone(userVo.getPhone())
                .setEmail(userVo.getEmail())
                .setStatus(toProtoStatus(userVo.getStatus()))
                .setRegisterIp(userVo.getRegisterIp())
                .setLastLoginTime(userVo.getLastLoginTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .setLastLoginIp(userVo.getLastLoginIp())
                .setCreateTime(userVo.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .setUpdateTime(userVo.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
    }

    private UserStatus toProtoStatus(com.zeeyeh.quyvera.user.enums.UserStatus status) {
        if (status == null) {
            return null;
        }
        return UserStatus.forNumber(status.ordinal());
    }
}

package com.zeeyeh.quyvera.plugin.grpc;

import com.zeeyeh.quyvera.plugin.api.v1.*;
import com.zeeyeh.quyvera.plugin.service.PluginService;
import com.zeeyeh.quyvera.plugin.vo.PluginVo;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.ZoneId;
import java.util.List;

/**
 * 插件远程调用实现类
 * @author Qinloren
 * @date 2026/10/26 18:46
 */
@GrpcService
public class PluginGrpcService extends PluginServiceGrpc.PluginServiceImplBase {
    private final PluginService pluginService;

    public PluginGrpcService(PluginService pluginService) {
        super();
        this.pluginService = pluginService;
    }

    @Override
    public void getPluginInfoById(GetPluginInfoByIdRequest request, StreamObserver<GetPluginInfoByIdResponse> responseObserver) {
        PluginVo pluginVo = pluginService.getPluginById(request.getPluginId());
        responseObserver.onNext(GetPluginInfoByIdResponse.newBuilder()
                        .setPluginInfo(toPluginInfo(pluginVo))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPluginInfoByName(GetPluginInfoByNameRequest request, StreamObserver<GetPluginInfoByNameResponse> responseObserver) {
        PluginVo pluginVo = pluginService.getPluginByName(request.getPluginName());
        responseObserver.onNext(GetPluginInfoByNameResponse.newBuilder()
                .setPluginInfo(toPluginInfo(pluginVo))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPluginInfoByCode(GetPluginInfoByCodeRequest request, StreamObserver<GetPluginInfoByCodeResponse> responseObserver) {
        PluginVo pluginVo = pluginService.getPluginByCode(request.getPluginCode());
        responseObserver.onNext(GetPluginInfoByCodeResponse.newBuilder()
                .setPluginInfo(toPluginInfo(pluginVo))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void batchGetPluginInfoByIds(BatchGetPluginInfoByIdsRequest request, StreamObserver<BatchGetPluginInfoByIdsResponse> responseObserver) {
        List<PluginVo> list = pluginService.listPluginByIds(request.getPluginIdsList());
        List<PluginInfo> result = list.stream()
                .map(this::toPluginInfo)
                .toList();
        responseObserver.onNext(BatchGetPluginInfoByIdsResponse.newBuilder()
                .addAllPluginInfos(result)
                .build());
    }

    private PluginInfo toPluginInfo(PluginVo pluginVo) {
        return PluginInfo.newBuilder()
                .setId(pluginVo.getId())
                .setPluginCode(pluginVo.getPluginCode())
                .setPluginName(pluginVo.getPluginName())
                .setFilename(pluginVo.getFilename())
                .setDescription(pluginVo.getDescription())
                .setAuthor(pluginVo.getAuthor())
                .setIsEnabled(pluginVo.getIsEnabled())
                .setInstallTime(pluginVo.getInstallTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .setCreateTime(pluginVo.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .setUpdateTime(pluginVo.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
    }
}

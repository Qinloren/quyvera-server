package com.zeeyeh.quyvera.plugin.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zeeyeh.quyvera.common.core.exception.BusinessExceptions;
import com.zeeyeh.quyvera.common.core.utils.BusinessAsserts;
import com.zeeyeh.quyvera.plugin.enums.PluginErrorCode;
import com.zeeyeh.quyvera.plugin.dto.PluginInstallDto;
import com.zeeyeh.quyvera.plugin.convert.PluginConvert;
import com.zeeyeh.quyvera.plugin.domain.Plugin;
import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import com.zeeyeh.quyvera.plugin.sdk.entity.config.PluginMetaLoader;
import com.zeeyeh.quyvera.plugin.service.PluginService;
import com.zeeyeh.quyvera.plugin.mapper.PluginMapper;
import com.zeeyeh.quyvera.plugin.vo.PluginVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * 插件表业务接口实现
 * @author Qinloren
 * @date  2026-10-25 22:12:13
 */
@Slf4j
@Service
@RefreshScope
public class PluginServiceImpl extends ServiceImpl<PluginMapper, Plugin> implements PluginService {

    private final Snowflake snowflake;
    private final PluginConvert pluginConvert;
    @Value("${app.plugin.path:./runtime/plugins}")
    private String pluginUploadDir;


    public PluginServiceImpl(Snowflake snowflake, PluginConvert pluginConvert) {
        this.snowflake = snowflake;
        this.pluginConvert = pluginConvert;
    }

    @Override
    public PluginVo installPlugin(PluginInstallDto dto, MultipartFile file) {
        BusinessAsserts.check(file == null || file.isEmpty(), PluginErrorCode.PLUGIN_FILE_NOT_EMPTY);
        File pluginFile = savePluginFile(dto.getPluginCode(), file);
        PluginMeta pluginMeta = PluginMetaLoader.load(pluginFile);
        if (pluginMeta == null) {
            log.error("插件元数据加载失败: {} => {}", dto.getPluginCode(), file.getOriginalFilename());
            throw new BusinessExceptions(PluginErrorCode.PLUGIN_META_LOAD_FAILED);
        }
        Plugin plugin = new Plugin();
        plugin.setId(snowflake.nextId());
        plugin.setPluginCode(dto.getPluginCode());
        plugin.setPluginName(pluginMeta.getName());
        plugin.setVersion(pluginMeta.getVersion().toString());
        plugin.setDescription(dto.getDescription());
        plugin.setAuthor(StringUtils.join(pluginMeta.getAuthors(), ","));
        plugin.setIsEnabled(false);
        plugin.setInstallTime(LocalDateTime.now());
        plugin.setInstallTime(LocalDateTime.now());
        plugin.setCreateTime(LocalDateTime.now());
        plugin.setUpdateTime(LocalDateTime.now());
        this.save(plugin);
        return pluginConvert.toVo(plugin);
    }

    /**
     * 保存插件文件到指定目录
     * @param pluginCode 插件编码
     * @param file 插件文件对象
     */
    private File savePluginFile(String pluginCode, MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        filename = Paths.get(filename).normalize().toString();
        String ext = "";
        int filenameDot = filename.lastIndexOf('.');
        if (filenameDot >= 0) ext = filename.substring(filenameDot + 1).toLowerCase(Locale.ROOT);
        BusinessAsserts.check(!ext.equalsIgnoreCase("jar"), PluginErrorCode.PLUGIN_EXT_NOT_SUPPORTED);
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path pluginFolderPath = Paths.get(pluginUploadDir).toAbsolutePath().normalize();
        Path target = pluginFolderPath.resolve(storedName).normalize();
        BusinessAsserts.check(!target.startsWith(pluginFolderPath), PluginErrorCode.PLUGIN_PATH_INVALID);
        try {
            Files.createDirectories(pluginFolderPath);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("插件上传失败: {} => {}", pluginCode, file.getOriginalFilename(), e);
            throw new BusinessExceptions(PluginErrorCode.PLUGIN_UPLOAD_FAILED);
        }
        return target.toFile();
    }
}





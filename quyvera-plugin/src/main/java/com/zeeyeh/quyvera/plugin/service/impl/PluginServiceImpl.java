package com.zeeyeh.quyvera.plugin.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zeeyeh.quyvera.common.core.entity.PageResult;
import com.zeeyeh.quyvera.common.core.exception.BusinessExceptions;
import com.zeeyeh.quyvera.common.core.utils.BusinessAsserts;
import com.zeeyeh.quyvera.plugin.dto.PluginPageQueryDto;
import com.zeeyeh.quyvera.plugin.enums.PluginErrorCode;
import com.zeeyeh.quyvera.plugin.dto.PluginInstallDto;
import com.zeeyeh.quyvera.plugin.convert.PluginConvert;
import com.zeeyeh.quyvera.plugin.domain.Plugin;
import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import com.zeeyeh.quyvera.plugin.sdk.entity.config.PluginMetaLoader;
import com.zeeyeh.quyvera.plugin.service.PluginService;
import com.zeeyeh.quyvera.plugin.mapper.PluginMapper;
import com.zeeyeh.quyvera.plugin.service.plugin.loader.PluginLoader;
import com.zeeyeh.quyvera.plugin.service.plugin.manager.PluginManager;
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
import java.util.List;
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
    private final PluginManager pluginManager;
    @Value("${app.plugin.plugin-folder:./plugins/file}")
    private String pluginUploadDir;
    @Value("${app.plugin.runtime-folder:./plugins/runtime}")
    private String pluginRuntimeDir;


    public PluginServiceImpl(Snowflake snowflake, PluginConvert pluginConvert, PluginManager pluginManager) {
        this.snowflake = snowflake;
        this.pluginConvert = pluginConvert;
        this.pluginManager = pluginManager;
    }

    @Override
    public PluginVo getPluginById(Long id) {
        Plugin plugin = this.getById(id);
        BusinessAsserts.check(plugin == null, PluginErrorCode.PLUGIN_NOT_FOUND);
        return pluginConvert.toVo(plugin);
    }

    @Override
    public PluginVo getPluginByName(String name) {
        Plugin plugin = this.lambdaQuery()
                .eq(Plugin::getPluginName, name)
                .one();
        BusinessAsserts.check(plugin == null, PluginErrorCode.PLUGIN_NOT_FOUND);
        return pluginConvert.toVo(plugin);
    }

    @Override
    public PluginVo getPluginByCode(String code) {
        Plugin plugin = this.lambdaQuery()
                .eq(Plugin::getPluginCode, code)
                .one();
        BusinessAsserts.check(plugin == null, PluginErrorCode.PLUGIN_NOT_FOUND);
        return pluginConvert.toVo(plugin);
    }

    @Override
    public List<PluginVo> listPluginByIds(List<Long> pluginIds) {
        List<Plugin> list = this.lambdaQuery()
                .in(Plugin::getId, pluginIds)
                .list();
        return pluginConvert.toVoList(list);
    }

    @Override
    public PageResult<PluginVo> pageQuery(PluginPageQueryDto dto) {
        int pageNumber = Math.max(0, dto.getPage() - 1);
        Page<Plugin> page = Page.of(pageNumber, dto.getSize());
        page.addOrder(OrderItem.asc(dto.getSort()));
        Page<Plugin> pluginPage = this.lambdaQuery()
                .eq(dto.getId() != null, Plugin::getId, dto.getId())
                .like(StringUtils.isNotBlank(dto.getPluginCode()), Plugin::getPluginCode, dto.getPluginCode())
                .like(StringUtils.isNotBlank(dto.getPluginName()), Plugin::getPluginName, dto.getPluginName())
                .like(StringUtils.isNotBlank(dto.getFilename()), Plugin::getFilename, dto.getFilename())
                .like(StringUtils.isNotBlank(dto.getVersion()), Plugin::getVersion, dto.getVersion())
                .like(StringUtils.isNotBlank(dto.getDescription()), Plugin::getDescription, dto.getDescription())
                .like(StringUtils.isNotBlank(dto.getAuthor()), Plugin::getAuthor, dto.getAuthor())
                .eq(dto.getIsEnabled() != null, Plugin::getIsEnabled, dto.getIsEnabled())
                .ge(dto.getInstallTimeStart() != null, Plugin::getInstallTime, dto.getInstallTimeStart())
                .le(dto.getInstallTimeEnd() != null, Plugin::getInstallTime, dto.getInstallTimeEnd())
                .page(page);
        List<Plugin> list = pluginPage.getRecords();
        List<PluginVo> result = pluginConvert.toVoList(list);
        return new PageResult<>(
                result,
                pluginPage.getCurrent(),
                pluginPage.getSize(),
                pluginPage.getTotal(),
                pluginPage.getPages(),
                pluginPage.getCurrent() == 0,
                pluginPage.getCurrent() == pluginPage.getTotal(),
                pluginPage.hasPrevious(),
                pluginPage.hasNext()
        );
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
        plugin.setFilename(pluginFile.getName());
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

    @Override
    public PluginVo enablePluginById(Long id) {
        PluginVo pluginVo = this.getPluginById(id);
        BusinessAsserts.check(pluginVo.getIsEnabled(), PluginErrorCode.PLUGIN_ALREADY_ENABLED);
        pluginVo = updatePluginStatus(id, Boolean.TRUE);
        BusinessAsserts.check(pluginManager.isLoaded(pluginVo.getPluginName()), PluginErrorCode.PLUGIN_ALREADY_ENABLED);
        return loadPluginFile(pluginVo);
    }

    @Override
    public PluginVo enablePluginByCode(String code) {
        PluginVo pluginVo = this.getPluginByCode(code);
        BusinessAsserts.check(pluginVo.getIsEnabled(), PluginErrorCode.PLUGIN_ALREADY_ENABLED);
        pluginVo = updatePluginStatus(code, Boolean.TRUE);
        BusinessAsserts.check(pluginManager.isLoaded(pluginVo.getPluginName()), PluginErrorCode.PLUGIN_ALREADY_ENABLED);
        return loadPluginFile(pluginVo);
    }

    private PluginVo loadPluginFile(PluginVo pluginVo) {
        Path pluginFolderPath = Paths.get(pluginUploadDir).toAbsolutePath().normalize();
        Path pluginFilePath = pluginFolderPath.resolve(pluginVo.getFilename()).toAbsolutePath().normalize();
        Path runtimeFolderPath = Paths.get(pluginRuntimeDir).toAbsolutePath().normalize();
        Path pluginRuntimeFolder = runtimeFolderPath.resolve(pluginVo.getPluginName()).toAbsolutePath().normalize();
        PluginMeta pluginMeta = PluginMetaLoader.load(pluginFilePath.toFile());
        pluginManager.load(new PluginLoader.PluginSource(
                pluginMeta,
                pluginFilePath,
                pluginRuntimeFolder
        ));
        return pluginVo;
    }

    @Override
    public PluginVo disablePluginById(Long id) {
        PluginVo pluginVo = this.getPluginById(id);
        BusinessAsserts.check(pluginVo.getIsEnabled(), PluginErrorCode.PLUGIN_ALREADY_DISABLED);
        pluginVo = updatePluginStatus(id, Boolean.FALSE);
        BusinessAsserts.check(!pluginManager.isLoaded(pluginVo.getPluginName()), PluginErrorCode.PLUGIN_ALREADY_DISABLED);
        pluginManager.unload(pluginVo.getPluginName());
        return pluginVo;
    }

    @Override
    public PluginVo disablePluginByCode(String code) {
        PluginVo pluginVo = this.getPluginByCode(code);
        BusinessAsserts.check(pluginVo.getIsEnabled(), PluginErrorCode.PLUGIN_ALREADY_DISABLED);
        pluginVo = updatePluginStatus(code, Boolean.FALSE);
        BusinessAsserts.check(!pluginManager.isLoaded(pluginVo.getPluginName()), PluginErrorCode.PLUGIN_ALREADY_DISABLED);
        pluginManager.unload(pluginVo.getPluginName());
        return pluginVo;
    }

    @Override
    public PluginVo updatePluginStatus(Long id, Boolean status) {
        BusinessAsserts.check(this.getById(id) == null, PluginErrorCode.PLUGIN_NOT_FOUND);
        boolean updated = this.lambdaUpdate()
                .set(Plugin::getIsEnabled, status)
                .eq(Plugin::getId, id)
                .update();
        BusinessAsserts.check(!updated, PluginErrorCode.PLUGIN_ENABLE_FAILED);
        Plugin plugin = this.getById(id);
        return pluginConvert.toVo(plugin);
    }

    @Override
    public PluginVo updatePluginStatus(String code, Boolean status) {
        Plugin plugin = this.lambdaQuery()
                .eq(Plugin::getPluginCode, code)
                .one();
        BusinessAsserts.check(plugin == null, PluginErrorCode.PLUGIN_NOT_FOUND);
        boolean updated = this.lambdaUpdate()
                .set(Plugin::getIsEnabled, status)
                .eq(Plugin::getPluginCode, code)
                .update();
        BusinessAsserts.check(!updated, PluginErrorCode.PLUGIN_ENABLE_FAILED);
        plugin = this.lambdaQuery()
                .eq(Plugin::getPluginCode, code)
                .one();
        return pluginConvert.toVo(plugin);
    }

    @Override
    public void unloadPluginById(Long id) {
        Plugin plugin = this.getById(id);
        BusinessAsserts.check(plugin == null, PluginErrorCode.PLUGIN_NOT_FOUND);
        this.disablePluginById(id);
        unloadPluginFile(plugin);
    }

    @Override
    public void unloadPluginByCode(String code) {
        Plugin plugin = this.lambdaQuery()
                .eq(Plugin::getPluginCode, code)
                .one();
        BusinessAsserts.check(plugin == null, PluginErrorCode.PLUGIN_NOT_FOUND);
        this.disablePluginByCode(code);
        unloadPluginFile(plugin);
    }

    /**
     * 卸载插件文件
     * @param plugin 插件对象
     */
    @Override
    public void unloadPluginFile(Plugin plugin) {
        String filename = plugin.getFilename();
        Path pluginFolderPath = Paths.get(pluginUploadDir).toAbsolutePath().normalize();
        Path pluginFilePath = pluginFolderPath.resolve(filename).normalize();
        FileUtil.rename(
                pluginFilePath.toFile(),
                filename + ".unload." + UUID.randomUUID().toString().replace("-", ""),
                false,
                true
        );
    }
}





package com.zeeyeh.quyvera.plugin;

import com.google.common.io.FileBackedOutputStream;
import com.zeeyeh.quyvera.common.core.entity.PageResult;
import com.zeeyeh.quyvera.plugin.dto.PluginPageQueryDto;
import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import com.zeeyeh.quyvera.plugin.sdk.entity.config.PluginMetaLoader;
import com.zeeyeh.quyvera.plugin.service.PluginService;
import com.zeeyeh.quyvera.plugin.service.plugin.loader.PluginLoader;
import com.zeeyeh.quyvera.plugin.service.plugin.manager.PluginManager;
import com.zeeyeh.quyvera.plugin.vo.PluginVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目启动后启动插件
 * @author Qinloren
 * @date 2026/11/15 20:28
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.plugin",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PluginStartupRunner implements ApplicationRunner {
    private final PluginManager pluginManager;
    private final PluginService pluginService;
    @Value("${app.plugin.plugin-folder}")
    private String pluginFolder;
    @Value("${app.plugin.runtime-folder}")
    private String runtimeFolder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<PluginLoader.PluginSource> sources = loadEnabledPlugins();
        pluginManager.start(sources);
    }

    private List<PluginLoader.PluginSource> loadEnabledPlugins() {
        PageResult<PluginVo> pagedQuery = pluginService.pageQuery(PluginPageQueryDto.builder()
                .isEnabled(true)
                .build());
        Path pluginRoot = Paths.get(pluginFolder).toAbsolutePath().normalize();
        Path runtimeRoot = Paths.get(runtimeFolder).toAbsolutePath().normalize();
        List<PluginLoader.PluginSource> sources = new ArrayList<>();
        for (PluginVo pluginVo : pagedQuery.getResults()) {
            if (!Boolean.TRUE.equals(pluginVo.getIsEnabled())) {
                continue;
            }
            Path jarFilePath = pluginRoot.resolve(pluginVo.getFilename()).normalize();
            if (!jarFilePath.startsWith(pluginRoot) || !Files.isRegularFile(jarFilePath)) {
                throw new IllegalStateException("插件 Jar 不存在或路径不合法: " + pluginVo.getFilename());
            }
            PluginMeta meta = PluginMetaLoader.load(jarFilePath.toFile());
            if (meta == null) {
                throw new IllegalStateException("插件元数据读取失败: " + pluginVo.getPluginName());
            }
            if (!pluginVo.getPluginName().equals(meta.getName())) {
                throw new IllegalStateException("插件编码与 JAR 元数据不一致: " + pluginVo.getPluginName());
            }
            Path runtimePath = runtimeRoot.resolve(pluginVo.getPluginName()).normalize();
            if (!runtimePath.startsWith(runtimeRoot)) {
                throw new IllegalStateException("插件运行目录非法: " + runtimePath);
            }
            sources.add(new PluginLoader.PluginSource(meta, jarFilePath, runtimePath));
        }
        return List.copyOf(sources);
    }
}

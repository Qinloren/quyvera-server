package com.zeeyeh.quyvera.plugin.service.plugin.scanner;

import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import com.zeeyeh.quyvera.plugin.sdk.entity.config.PluginMetaLoader;
import com.zeeyeh.quyvera.plugin.service.plugin.loader.PluginLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * 插件文件扫描器
 * @author Qinloren
 * @date 2026/11/15 15:34
 */
@Slf4j
public class PluginScanner {

    private final Path pluginDirectory;
    private final Path runtimeDirectory;

    public PluginScanner(Path pluginDirectory, Path runtimeDirectory) {
        this.pluginDirectory = Objects.requireNonNull(pluginDirectory, "pluginDirectory").toAbsolutePath().normalize();
        this.runtimeDirectory = Objects.requireNonNull(runtimeDirectory, "runtimeDirectory").toAbsolutePath().normalize();
    }

    public List<PluginLoader.PluginSource> scan() throws IOException {
        if (!Files.exists(pluginDirectory)) {
            Files.createDirectories(pluginDirectory);
            log.info("插件目录不存在，已创建: {}", pluginDirectory);
        }
        if (!Files.isDirectory(pluginDirectory)) {
            throw new IllegalStateException("插件路径不是目录: %s".formatted(pluginDirectory));
        }
        Files.createDirectories(runtimeDirectory);
        List<Path> jarFiles;
        try (Stream<Path> stream = Files.list(pluginDirectory)) {
            jarFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(this::isJarFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }
        List<PluginLoader.PluginSource> sources = new ArrayList<>();
        Set<String> pluginNames = new HashSet<>();
        for (Path jarFile : jarFiles) {
            PluginMeta meta = PluginMetaLoader.load(jarFile.toFile());
            if (meta == null) {
                log.warn("跳过没有有效元数据的jar文件： {}", jarFile);
                continue;
            }
            String pluginName = meta.getName();
            if (!pluginNames.add(pluginName)) {
                throw new IllegalStateException("发现重复插件名称: %s".formatted(pluginName));
            }
            Path pluginRuntimeFolder = runtimeDirectory.resolve(pluginName).normalize();
            if (!pluginRuntimeFolder.startsWith(runtimeDirectory)) {
                throw new IllegalStateException("插件运行时目录非法: %s".formatted(pluginName));
            }
            sources.add(new PluginLoader.PluginSource(meta, jarFile, pluginRuntimeFolder));
            log.info("发现插件: {}, 文件: {}", meta.getFullName(), jarFile);
        }
        for (PluginLoader.PluginSource source : sources) {
            Files.createDirectories(source.runtimeFolder());
        }
        log.info("插件扫描完成，共发现 {} 个插件", sources.size());
        return List.copyOf(sources);
    }

    private boolean isJarFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.toLowerCase().endsWith(".jar");
    }
}

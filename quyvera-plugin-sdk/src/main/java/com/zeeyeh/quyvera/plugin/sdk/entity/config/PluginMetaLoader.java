package com.zeeyeh.quyvera.plugin.sdk.entity.config;

import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 插件元数据加载器
 * @author Qinloren
 * @date 2026/10/25 23:19
 */
@Slf4j
public class PluginMetaLoader {

    private static final String PLUGIN_META_FILE_NAME = "plugin.json";

    /**
     * 从文件中加载插件元数据
     * @param file 插件文件
     * @return 插件元数据对象
     */
    public static PluginMeta load(File file) {
        String pluginMetaString = readPluginMetaFile(file);
        if (StringUtils.isBlank(pluginMetaString)) {
            return null;
        }
        return new PluginMeta(pluginMetaString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从jar文件中读取插件元数据文件
     * @param file jar文件
     * @return 插件元数据字符串
     */
    private static String readPluginMetaFile(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            JarEntry jarEntry = jarFile.getJarEntry(PLUGIN_META_FILE_NAME);
            if (jarEntry == null || jarEntry.isDirectory()) {
                return null;
            }
            try (InputStream inputStream = jarFile.getInputStream(jarEntry);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append(System.lineSeparator());
                }
                return builder.toString();
            }
        } catch (IOException e) {
            log.error("插件元数据文件读取失败: ", e);
            return null;
        }
    }
}

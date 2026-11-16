package com.zeeyeh.quyvera.plugin.service.plugin.library;

import java.net.URL;
import java.util.List;

/**
 * 插件依赖解析器接口
 * @author Qinloren
 * @date 2026/11/15 13:14
 */
public interface PluginLibraryResolver {

    /**
     * 解析插件声明的 Maven 依赖。
     *
     * @param pluginName 插件名称
     * @param libraries  Maven 坐标列表
     * @return 依赖 JAR 的 URL 列表，包含传递依赖
     */
    List<URL> resolve(String pluginName, List<String> libraries);
}

package com.zeeyeh.quyvera.plugin.sdk;

import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import lombok.Data;

import java.io.File;

/**
 * 项目插件抽象类
 * @author Qinloren
 * @date 2026/10/25 17:59
 */
@Data
public abstract class AbstractPlugin {
    /**
     * 插件程序运行时目录
     */
    private File runtimeFolder;

    /**
     * 插件元数据信息
     */
    private PluginMeta pluginMeta;

    protected AbstractPlugin(File runtimeFolder) {
        this.runtimeFolder = runtimeFolder;
    }

    /**
     * 获取插件国际化目录
     * @return 插件国际化目录
     */
    public final File getLocaleFolder() {
        return new File(runtimeFolder, "locale");
    }

    /**
     * 获取插件配置目录
     * @return 插件配置目录
     */
    public final File getConfigFolder() {
        return new File(runtimeFolder, "config");
    }

    /**
     * 获取插件数据目录
     * @return 插件数据目录
     */
    public final File getDataFolder() {
        return new File(runtimeFolder, "data");
    }

    /**
     * 插件初始化时执行
     */
    public abstract void init();

    /**
     * 插件加载时执行
     */
    public abstract void load();

    /**
     * 插件卸载时执行
     */
    public abstract void unload();

    @Override
    public String toString() {
        return pluginMeta.getFullName();
    }
}

package com.zeeyeh.quyvera.plugin.sdk;

import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.EventRegistry;
import lombok.Data;

import java.io.File;
import java.util.*;

/**
 * 项目插件抽象类
 *
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
    private String pluginName;
    private EventRegistry eventRegistry;

    public AbstractPlugin(File runtimeFolder) {
        this.runtimeFolder = runtimeFolder;
    }

    public final void attachMeta(PluginMeta meta) {
        if (this.pluginMeta != null) {
            throw new IllegalStateException("插件元数据重复注入: " + pluginName());
        }
        this.pluginMeta = Objects.requireNonNull(meta, "meta");
    }

    /**
     * 插件名，未注入元数据时退化为运行时目录名
     * @return 插件名
     */
    public final String pluginName() {
        String name = this.pluginName;
        if (name != null) {
            return name;
        }
        PluginMeta meta = this.pluginMeta;
        return meta == null ? null : meta.getName();
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
     * 声明本插件对外暴露、允许被远程触发的事件类型
     * <p>
     * 插件服务加载插件后会遍历本方法返回值，把这些类型登记到远程分发器上，
     * 从而允许其他微服务通过 gRPC 触发。默认不暴露任何类型；
     * 需要被远程触发的事件重写本方法即可，无需依赖任何中间件类型。
     *
     * @return 对外暴露的事件类型，默认空列表
     */
    public List<Class<? extends Event>> remoteEventTypes() {
        return Collections.emptyList();
    }

    /**
     * 对外暴露的事件逻辑名，默认取类全限定名
     * <p>
     * 其他微服务通过这个名称触发事件，建议重写为稳定的业务名（如 {@code order.created}），
     * 这样重构包名或类名不会破坏调用方契约。
     *
     * @param eventType 事件类型
     * @return 逻辑事件名
     */
    public String remoteEventName(Class<? extends Event> eventType) {
        return eventType.getName();
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
        return pluginMeta == null ? pluginName() : pluginMeta.getFullName();
    }

    public final void attachEventRegistry(EventRegistry eventRegistry) {
        Objects.requireNonNull(eventRegistry, "eventRegistry");
        if (this.eventRegistry != null) {
            throw new IllegalStateException("插件 %s 事件注册器重复绑定".formatted(pluginName()));
        }
        this.eventRegistry = eventRegistry;
    }

    protected final EventRegistry events() {
        if (eventRegistry == null) {
            throw new IllegalStateException("插件 %s 事件注册器尚未初始化".formatted(pluginName()));
        }
        return eventRegistry;
    }
}

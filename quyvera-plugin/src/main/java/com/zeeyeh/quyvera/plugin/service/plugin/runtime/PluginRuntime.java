package com.zeeyeh.quyvera.plugin.service.plugin.runtime;

import com.zeeyeh.quyvera.plugin.sdk.AbstractPlugin;
import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import com.zeeyeh.quyvera.plugin.sdk.enums.PluginState;
import com.zeeyeh.quyvera.plugin.service.plugin.loader.PluginClassLoader;
import lombok.Getter;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 插件运行时
 * @author Qinloren
 * @date 2026/11/15 14:10
 */
@Getter
public class PluginRuntime {

    private final PluginMeta meta;
    private final Path pluginFile;
    private final File runtimeFolder;
    private final PluginClassLoader classLoader;
    private volatile AbstractPlugin plugin;
    private final AtomicReference<PluginState> state = new AtomicReference<>(PluginState.DISCOVERED);
    public PluginRuntime(PluginMeta meta, Path pluginFile, File runtimeFolder, PluginClassLoader classLoader) {
        this.meta = Objects.requireNonNull(meta, "meta");
        this.pluginFile = Objects.requireNonNull(pluginFile, "pluginFile").toAbsolutePath().normalize();
        this.runtimeFolder = Objects.requireNonNull(runtimeFolder, "runtimeFolder").getAbsoluteFile();
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    public synchronized void setPlugin(AbstractPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        if (this.plugin != null) {
            throw new IllegalStateException("插件实例重复设置: %s".formatted(meta.getName()));
        }
        this.plugin = plugin;
    }

    public boolean transition(PluginState expected, PluginState next) {
        Objects.requireNonNull(expected, "expected");
        Objects.requireNonNull(next, "next");
        return state.compareAndSet(expected, next);
    }

    public void markFailed() {
        state.set(PluginState.FAILED);
    }

    public String getName() {
        return meta.getName();
    }

    @Override
    public String toString() {
        return meta.getFullName() + "[" + state.get() + "]";
    }
}

package com.zeeyeh.quyvera.plugin.service.plugin.loader;

import com.zeeyeh.quyvera.plugin.sdk.AbstractPlugin;
import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import com.zeeyeh.quyvera.plugin.sdk.exceptions.InvalidPluginException;
import com.zeeyeh.quyvera.plugin.service.plugin.dependency.PluginDependencyManager;
import com.zeeyeh.quyvera.plugin.service.plugin.library.PluginLibraryResolver;
import com.zeeyeh.quyvera.plugin.service.plugin.runtime.PluginRuntime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 插件加载器
 * @author Qinloren
 * @date 2026/11/15 14:20
 */
public final class PluginLoader {
    private final PluginDependencyManager dependencyManager;
    private final PluginLibraryResolver libraryResolver;
    private final ClassLoader parentClassLoader;
    public PluginLoader(PluginDependencyManager dependencyManager, PluginLibraryResolver libraryResolver) {
        this.dependencyManager = Objects.requireNonNull(dependencyManager, "dependencyManager");
        this.libraryResolver = Objects.requireNonNull(libraryResolver, "libraryResolver");
        this.parentClassLoader = AbstractPlugin.class.getClassLoader();
    }

    public Map<String, PluginRuntime> loadAll(List<PluginSource> sources) {
        Objects.requireNonNull(sources, "sources");
        if (sources.isEmpty()) {
            return Map.of();
        }
        List<PluginMeta> metas = sources.stream()
                .map(PluginSource::meta)
                .toList();
        PluginDependencyManager.Plan plan = dependencyManager.resolve(metas);
        Map<String, PluginSource> sourceMap = new LinkedHashMap<>();
        for (PluginSource source : sources) {
            String pluginName = source.meta().getName();
            PluginSource previous = sourceMap.putIfAbsent(pluginName, source);
            if (previous != null) {
                throw new InvalidPluginException("Duplicate plugin name: %s".formatted(pluginName));
            }
        }
        Map<String, PluginClassLoader> classLoaders = new LinkedHashMap<>();
        Map<String, PluginRuntime> runtimes = new LinkedHashMap<>();
        try {
            createClassLoaders(plan, sourceMap, classLoaders);
            dependencyManager.initializeClassLoaders(plan, classLoaders);
            instantiatePlugins(plan, sourceMap, classLoaders, runtimes);
            return Collections.unmodifiableMap(new LinkedHashMap<>(runtimes));
        } catch (Throwable failure) {
            cleanup(runtimes, classLoaders, failure);
            if (failure instanceof Error error) {
                throw error;
            }
            if (failure instanceof RuntimeException exception) {
                throw exception;
            }
            throw new InvalidPluginException("Failed to load plugins", failure);
        }
    }
    /**
     * 动态加载单个插件。
     *
     * <p>
     * 只创建新插件自己的 ClassLoader，
     * 已运行插件的 ClassLoader 直接复用。
     * </p>
     *
     * <p>
     * 此方法只负责插件实例化，不执行 init() 和 load()。
     * </p>
     *
     * @param source 新插件来源
     * @param loadedPlugins 当前已加载的插件
     * @return 新插件运行时
     */
    public PluginRuntime loadOne(PluginSource source, Map<String, PluginRuntime> loadedPlugins) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(loadedPlugins, "loadedPlugins");

        String pluginName = source.meta().getName();

        // 不允许重复加载。
        if (loadedPlugins.containsKey(pluginName)) {
            throw new InvalidPluginException(
                    "Plugin already loaded: " + pluginName
            );
        }

        // =====================================================
        // 1. 解析依赖
        // =====================================================

        List<PluginMeta> metas = new ArrayList<>();

        for (PluginRuntime runtime : loadedPlugins.values()) {
            metas.add(runtime.getMeta());
        }

        metas.add(source.meta());

        PluginDependencyManager.Plan plan =
                dependencyManager.resolve(metas);

        // 只获取新插件的直接依赖。
        List<String> dependencyNames =
                plan.dependenciesOf(pluginName);

        List<PluginClassLoader> dependencies = new ArrayList<>();

        for (String dependencyName : dependencyNames) {

            PluginRuntime dependencyRuntime =
                    loadedPlugins.get(dependencyName);

            if (dependencyRuntime == null) {
                throw new InvalidPluginException(
                        "Dependency plugin is not loaded: "
                                + dependencyName
                                + ", required by "
                                + pluginName
                );
            }

            PluginClassLoader dependencyLoader =
                    dependencyRuntime.getClassLoader();

            if (dependencyLoader == null || dependencyLoader.isClosed()) {
                throw new InvalidPluginException(
                        "Dependency ClassLoader is unavailable: "
                                + dependencyName
                );
            }

            dependencies.add(dependencyLoader);
        }

        // =====================================================
        // 2. 创建新插件 ClassLoader
        // =====================================================

        PluginClassLoader classLoader = null;

        try {

            Path pluginFile = source.pluginFile();

            if (!Files.isRegularFile(pluginFile)) {
                throw new InvalidPluginException(
                        "Plugin file does not exist: " + pluginFile
                );
            }

            List<URL> libraryUrls = libraryResolver.resolve(
                    pluginName,
                    source.meta().getLibs()
            );

            if (libraryUrls == null) {
                throw new InvalidPluginException(
                        "Library resolver returned null for plugin: "
                                + pluginName
                );
            }

            LinkedHashSet<URL> urls = new LinkedHashSet<>();

            urls.add(pluginFile.toRealPath().toUri().toURL());
            urls.addAll(libraryUrls);

            classLoader = new PluginClassLoader(
                    pluginName,
                    urls.toArray(new URL[0]),
                    parentClassLoader
            );

            // =================================================
            // 3. 初始化新插件的依赖
            // =================================================

            classLoader.initializeDependencies(dependencies);

            // =================================================
            // 4. 实例化插件
            // =================================================

            File runtimeFolder = source.runtimeFolder()
                    .toAbsolutePath()
                    .normalize()
                    .toFile();

            Files.createDirectories(runtimeFolder.toPath());

            PluginMeta meta = source.meta();

            Class<?> mainClass = Class.forName(
                    meta.getMain(),
                    true,
                    classLoader
            );

            if (!AbstractPlugin.class.isAssignableFrom(mainClass)) {
                throw new InvalidPluginException(
                        "Plugin main class must extend AbstractPlugin: "
                                + meta.getMain()
                );
            }

            Class<? extends AbstractPlugin> pluginClass =
                    mainClass.asSubclass(AbstractPlugin.class);

            Constructor<? extends AbstractPlugin> constructor =
                    pluginClass.getConstructor(File.class);

            AbstractPlugin plugin = constructor.newInstance(runtimeFolder);

            PluginRuntime runtime = new PluginRuntime(
                    meta,
                    source.pluginFile(),
                    runtimeFolder,
                    classLoader
            );

            runtime.setPlugin(plugin);

            plugin.attachMeta(meta);

            return runtime;

        } catch (Throwable failure) {

            // 只清理本次新建的 ClassLoader。
            // 绝不能关闭已经运行的依赖插件 ClassLoader。
            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (Throwable cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }

            if (failure instanceof Error error) {
                throw error;
            }

            if (failure instanceof RuntimeException exception) {
                throw exception;
            }

            throw new InvalidPluginException(
                    "Failed to load plugin: " + pluginName,
                    failure
            );
        }
    }

    private void createClassLoaders(PluginDependencyManager.Plan plan, Map<String, PluginSource> sources, Map<String, PluginClassLoader> classLoaders) throws IOException {
        for (String pluginName : plan.loadOrder()) {
            PluginSource source = sources.get(pluginName);
            if (source == null) {
                throw new InvalidPluginException("Missing plugin source: %s".formatted(pluginName));
            }
            Path pluginFile = source.pluginFile();
            if (!Files.isRegularFile(pluginFile)) {
                throw new InvalidPluginException("plugin file does not exist: %s".formatted(pluginFile));
            }
            List<URL> libraryUrls = libraryResolver.resolve(pluginName, source.meta().getLibs());
            if (libraryUrls == null) {
                throw new InvalidPluginException("Library resolver returned null for plugin: %s".formatted(pluginName));
            }
            LinkedHashSet<URL> urls = new LinkedHashSet<>();
            urls.add(pluginFile.toRealPath().toUri().toURL());
            urls.addAll(libraryUrls);
            URL[] classPath = urls.toArray(new URL[0]);
            PluginClassLoader pluginClassLoader = new PluginClassLoader(pluginName, classPath, parentClassLoader);
            classLoaders.put(pluginName, pluginClassLoader);
        }
    }

    private void instantiatePlugins(PluginDependencyManager.Plan plan, Map<String, PluginSource> sources, Map<String, PluginClassLoader> classLoaders, Map<String, PluginRuntime> runtimes) throws ReflectiveOperationException {
        for (String pluginName : plan.loadOrder()) {
            PluginSource source = sources.get(pluginName);
            PluginMeta meta = source.meta();
            PluginClassLoader classLoader = classLoaders.get(pluginName);
            File runtimeFolder = source.runtimeFolder().toAbsolutePath().normalize().toFile();
            try {
                Files.createDirectories(runtimeFolder.toPath());
            } catch (IOException e) {
                throw new InvalidPluginException("Cannot create runtime folder for plugin: %s".formatted(pluginName), e);
            }
            Class<?> mainClass = Class.forName(meta.getMain(), true, classLoader);
            if (!AbstractPlugin.class.isAssignableFrom(mainClass)) {
                throw new InvalidPluginException("Plugin main class must extend AbstractPlugin: %s".formatted(meta.getMain()));
            }
            Class<? extends AbstractPlugin> pluginClass = mainClass.asSubclass(AbstractPlugin.class);
            Constructor<? extends AbstractPlugin> constructor = pluginClass.getConstructor(File.class);
            AbstractPlugin plugin = constructor.newInstance(runtimeFolder);
            PluginRuntime runtime = new PluginRuntime(meta, source.pluginFile(), runtimeFolder, classLoader);
            runtime.setPlugin(plugin);
            runtimes.put(pluginName, runtime);
            plugin.attachMeta(meta);
        }
    }


    private void cleanup(Map<String, PluginRuntime> runtimes, Map<String, PluginClassLoader> classLoaders, Throwable originalFailure) {
        for (PluginClassLoader loader : classLoaders.values()) {
            try {
                loader.close();
            } catch (Throwable cleanupFailure) {
                originalFailure.addSuppressed(cleanupFailure);
            }
        }
        runtimes.clear();
        classLoaders.clear();
    }

    public record PluginSource(PluginMeta meta, Path pluginFile, Path runtimeFolder) {
        public PluginSource {
            Objects.requireNonNull(meta, "meta");
            Objects.requireNonNull(pluginFile, "pluginFile");
            Objects.requireNonNull(runtimeFolder, "runtimeFolder");
            pluginFile = pluginFile.toAbsolutePath().normalize();
            runtimeFolder = runtimeFolder.toAbsolutePath().normalize();
        }
    }
}

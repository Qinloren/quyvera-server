package com.zeeyeh.quyvera.plugin.service.plugin.manager;

import com.zeeyeh.quyvera.plugin.sdk.AbstractPlugin;
import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import com.zeeyeh.quyvera.plugin.sdk.enums.PluginState;
import com.zeeyeh.quyvera.plugin.service.plugin.event.PluginEventRegistry;
import com.zeeyeh.quyvera.plugin.service.plugin.loader.PluginLoader;
import com.zeeyeh.quyvera.plugin.service.plugin.runtime.PluginRuntime;
import com.zeeyeh.quyvera.plugin.service.plugin.service.PluginServiceInvokerFactory;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 插件管理器。AI生成
 * @author Qinloren
 * @date 2026/11/15 14:57
 */
public final class PluginManager {

    /**
     * 串行化插件生命周期操作。
     *
     * <p>持有此锁时允许执行插件代码，
     * 但普通查询方法不获取此锁。</p>
     */
    private final ReentrantLock lifecycleLock = new ReentrantLock();

    /**
     * 保护管理器内部状态。
     *
     * <p>只允许短时间持有，
     * 禁止持有此锁执行插件代码或等待事件监听器。</p>
     */
    private final Object stateLock = new Object();

    private boolean started;

    private final PluginLoader pluginLoader;

    private final PluginServiceInvokerFactory serviceInvokerFactory;

    /**
     * 正常运行的插件。
     *
     * <p>只存放已经完成 init()、load() 和事件注册器激活的插件。</p>
     */
    private final Map<String, PluginRuntime> plugins =
            new LinkedHashMap<>();

    /**
     * 生命周期或资源清理失败的插件。
     */
    private final Map<String, PluginRuntime> failedPlugins =
            new LinkedHashMap<>();

    private final EventManager eventManager;

    /**
     * 插件对应的事件注册器。
     */
    private final Map<String, PluginEventRegistry> eventRegistries =
            new LinkedHashMap<>();

    public PluginManager(
            PluginLoader pluginLoader,
            EventManager eventManager,
            PluginServiceInvokerFactory serviceInvokerFactory
    ) {
        this.pluginLoader = Objects.requireNonNull(
                pluginLoader,
                "pluginLoader"
        );

        this.eventManager = Objects.requireNonNull(
                eventManager,
                "eventManager"
        );

        this.serviceInvokerFactory = Objects.requireNonNull(
                serviceInvokerFactory,
                "serviceInvokerFactory"
        );
    }

    /**
     * 批量启动插件。
     */
    public void start(List<PluginLoader.PluginSource> sources) {
        Objects.requireNonNull(sources, "sources");

        ensureNotInEventCallback();

        lifecycleLock.lock();

        try {
            synchronized (stateLock) {
                if (started) {
                    throw new IllegalStateException(
                            "插件管理器已启动"
                    );
                }

                if (!plugins.isEmpty()) {
                    throw new IllegalStateException(
                            "插件管理器状态异常：尚未启动但存在运行中的插件"
                    );
                }

                if (!eventRegistries.isEmpty()) {
                    throw new IllegalStateException(
                            "插件管理器状态异常：存在未清理的事件注册器"
                    );
                }

                for (PluginLoader.PluginSource source : sources) {
                    Objects.requireNonNull(source, "source");

                    PluginMeta meta = Objects.requireNonNull(
                            source.meta(),
                            "source.meta"
                    );

                    String pluginName = Objects.requireNonNull(
                            meta.getName(),
                            "pluginName"
                    );

                    if (failedPlugins.containsKey(pluginName)) {
                        throw new IllegalStateException(
                                "插件之前存在清理失败的运行时，无法启动: "
                                        + pluginName
                        );
                    }
                }
            }

            /*
             * 运行时先保存在局部集合中。
             *
             * 在所有插件成功完成初始化、加载和事件注册器激活之前，
             * 不放入 plugins 对外公布。
             */
            Map<String, PluginRuntime> runtimes =
                    pluginLoader.loadAll(sources);

            Objects.requireNonNull(
                    runtimes,
                    "pluginLoader.loadAll() returned null"
            );

            Set<String> lifecycleStarted = new HashSet<>();

            try {
                // 第一阶段：为所有插件绑定独立事件注册器。
                for (PluginRuntime runtime : runtimes.values()) {
                    attachEventRegistry(runtime);
                }

                // 第二阶段：初始化所有插件。
                for (PluginRuntime runtime : runtimes.values()) {
                    String pluginName = runtime.getName();

                    transition(
                            runtime,
                            PluginState.DISCOVERED,
                            PluginState.INITIALIZING
                    );

                    runtime.getPlugin().attachServiceInvoker(serviceInvokerFactory.create(runtime.getName()));

                    /*
                     * 在调用 init() 之前记录。
                     * 即使 init() 抛出异常，也需要执行 unload() 清理。
                     */
                    lifecycleStarted.add(pluginName);

                    runtime.getPlugin().init();

                    transition(
                            runtime,
                            PluginState.INITIALIZING,
                            PluginState.INITIALIZED
                    );
                }

                // 第三阶段：加载所有插件。
                for (PluginRuntime runtime : runtimes.values()) {
                    transition(
                            runtime,
                            PluginState.INITIALIZED,
                            PluginState.LOADING
                    );

                    runtime.getPlugin().load();

                    transition(
                            runtime,
                            PluginState.LOADING,
                            PluginState.LOADED
                    );
                }

                // 第四阶段：激活所有插件的事件注册器。
                for (PluginRuntime runtime : runtimes.values()) {
                    activateEventRegistry(runtime.getName());
                }

                /*
                 * 所有插件均完成加载并激活事件注册器后，
                 * 才统一发布到正常运行集合。
                 */
                synchronized (stateLock) {
                    for (String pluginName : runtimes.keySet()) {
                        if (plugins.containsKey(pluginName)
                                || failedPlugins.containsKey(pluginName)) {
                            throw new IllegalStateException(
                                    "插件运行时名称冲突: " + pluginName
                            );
                        }
                    }

                    plugins.putAll(runtimes);
                    started = true;
                }

            } catch (Throwable cause) {
                rollback(
                        runtimes,
                        lifecycleStarted,
                        cause
                );

                rethrow(cause, "插件启动失败");
            }

        } finally {
            lifecycleLock.unlock();
        }
    }

    /**
     * 动态加载单个插件。
     */
    public void load(PluginLoader.PluginSource source) {
        Objects.requireNonNull(source, "source");

        ensureNotInEventCallback();

        lifecycleLock.lock();

        try {
            PluginMeta meta = Objects.requireNonNull(
                    source.meta(),
                    "source.meta"
            );

            String pluginName = Objects.requireNonNull(
                    meta.getName(),
                    "pluginName"
            );

            Map<String, PluginRuntime> loadedPlugins = getLoadedPlugins(pluginName);

            // 只创建新插件的运行时实例，不执行生命周期方法。
            PluginRuntime runtime = pluginLoader.loadOne(
                    source,
                    loadedPlugins
            );

            boolean lifecycleStarted = false;

            try {
                attachEventRegistry(runtime);

                transition(
                        runtime,
                        PluginState.DISCOVERED,
                        PluginState.INITIALIZING
                );

                runtime.getPlugin().attachServiceInvoker(
                        serviceInvokerFactory.create(runtime.getName())
                );

                lifecycleStarted = true;

                runtime.getPlugin().init();

                transition(
                        runtime,
                        PluginState.INITIALIZING,
                        PluginState.INITIALIZED
                );

                transition(
                        runtime,
                        PluginState.INITIALIZED,
                        PluginState.LOADING
                );

                runtime.getPlugin().load();

                transition(
                        runtime,
                        PluginState.LOADING,
                        PluginState.LOADED
                );

                // 插件加载成功后再激活事件注册器。
                activateEventRegistry(pluginName);

                // 最后才对外公布插件运行时。
                synchronized (stateLock) {
                    if (plugins.containsKey(pluginName)
                            || failedPlugins.containsKey(pluginName)) {
                        throw new IllegalStateException(
                                "插件运行时名称冲突: " + pluginName
                        );
                    }

                    plugins.put(pluginName, runtime);
                }

            } catch (Throwable cause) {
                boolean cleanupFailed = cleanupPartialRuntime(
                        runtime,
                        lifecycleStarted,
                        cause
                );

                if (cleanupFailed) {
                    synchronized (stateLock) {
                        failedPlugins.put(pluginName, runtime);
                    }
                }

                rethrow(
                        cause,
                        "动态加载插件失败: " + pluginName
                );
            }

        } finally {
            lifecycleLock.unlock();
        }
    }

    private @NonNull Map<String, PluginRuntime> getLoadedPlugins(String pluginName) {
        Map<String, PluginRuntime> loadedPlugins;

        synchronized (stateLock) {
            if (!started) {
                throw new IllegalStateException(
                        "插件管理器尚未启动"
                );
            }

            if (plugins.containsKey(pluginName)) {
                throw new IllegalStateException(
                        "插件已经加载: " + pluginName
                );
            }

            if (failedPlugins.containsKey(pluginName)) {
                throw new IllegalStateException(
                        "插件之前存在清理失败的运行时，无法重新加载: "
                                + pluginName
                );
            }

            if (eventRegistries.containsKey(pluginName)) {
                throw new IllegalStateException(
                        "插件之前存在未清理的事件注册器: "
                                + pluginName
                );
            }

            // 创建快照，避免把内部可变集合传给加载器。
            loadedPlugins = new LinkedHashMap<>(plugins);
        }
        return loadedPlugins;
    }

    /**
     * 卸载指定插件。
     */
    public void unload(String pluginName) {
        Objects.requireNonNull(pluginName, "pluginName");

        ensureNotInEventCallback();

        lifecycleLock.lock();

        try {
            PluginRuntime runtime;

            Map<String, PluginRuntime> loadedPlugins;
            Map<String, PluginRuntime> failedSnapshot;

            synchronized (stateLock) {
                runtime = plugins.get(pluginName);

                if (runtime == null) {
                    throw new IllegalArgumentException(
                            "插件未加载: " + pluginName
                    );
                }

                loadedPlugins = new LinkedHashMap<>(plugins);
                failedSnapshot = new LinkedHashMap<>(failedPlugins);
            }

            /*
             * 检查正常运行插件和仍保留失败运行时的插件。
             * 避免仍持有依赖关系的失败插件被忽略。
             */
            checkDependents(
                    pluginName,
                    loadedPlugins,
                    failedSnapshot
            );

            try {
                unloadRuntime(runtime);

                synchronized (stateLock) {
                    plugins.remove(pluginName);
                }

            } catch (Throwable cause) {
                synchronized (stateLock) {
                    plugins.remove(pluginName);
                    failedPlugins.put(pluginName, runtime);
                }

                rethrow(
                        cause,
                        "卸载插件失败: " + pluginName
                );
            }

        } finally {
            lifecycleLock.unlock();
        }
    }

    /**
     * 关闭插件管理器。
     *
     * <p>按照加载顺序逆序卸载所有正常运行的插件。</p>
     */
    public void shutdown() {
        ensureNotInEventCallback();

        lifecycleLock.lock();

        try {
            List<PluginRuntime> reverseOrder;

            synchronized (stateLock) {
                reverseOrder = new ArrayList<>(plugins.values());
            }

            Collections.reverse(reverseOrder);

            Throwable firstFailure = null;

            try {
                for (PluginRuntime runtime : reverseOrder) {
                    String pluginName = runtime.getName();

                    try {
                        unloadRuntime(runtime);

                    } catch (Throwable cause) {
                        synchronized (stateLock) {
                            failedPlugins.put(pluginName, runtime);
                        }

                        if (firstFailure == null) {
                            firstFailure = cause;
                        } else {
                            addSuppressedSafely(
                                    firstFailure,
                                    cause
                            );
                        }

                    } finally {
                        synchronized (stateLock) {
                            plugins.remove(pluginName);
                        }
                    }
                }

            } finally {
                synchronized (stateLock) {
                    started = false;
                }
            }

            if (firstFailure != null) {
                rethrow(
                        firstFailure,
                        "关闭插件管理器时发生卸载失败"
                );
            }

        } finally {
            lifecycleLock.unlock();
        }
    }

    /**
     * 绑定插件事件注册器。
     */
    private void attachEventRegistry(PluginRuntime runtime) {
        String pluginName = runtime.getName();

        PluginEventRegistry registry =
                new PluginEventRegistry(
                        pluginName,
                        eventManager
                );

        synchronized (stateLock) {
            if (eventRegistries.containsKey(pluginName)) {
                throw new IllegalStateException(
                        "插件事件注册器已经存在: " + pluginName
                );
            }

            /*
             * 先保存引用，再执行注入。
             * 即使注入失败，后续也可以通过统一清理流程重试关闭。
             */
            eventRegistries.put(pluginName, registry);
        }

        try {
            runtime.getPlugin().attachEventRegistry(registry);

        } catch (RuntimeException | Error cause) {
            try {
                closeEventRegistry(pluginName);

            } catch (Throwable cleanupException) {
                addSuppressedSafely(
                        cause,
                        cleanupException
                );
            }

            throw cause;
        }
    }

    /**
     * 激活指定插件的事件注册器。
     */
    private void activateEventRegistry(String pluginName) {
        PluginEventRegistry registry;

        synchronized (stateLock) {
            registry = eventRegistries.get(pluginName);
        }

        if (registry == null) {
            throw new IllegalStateException(
                    "插件事件注册器不存在: " + pluginName
            );
        }

        registry.activate();
    }

    /**
     * 获取指定插件运行时。
     */
    public PluginRuntime getRuntime(String pluginName) {
        synchronized (stateLock) {
            return plugins.get(pluginName);
        }
    }

    /**
     * 获取指定插件实例。
     */
    public AbstractPlugin getPlugin(String pluginName) {
        synchronized (stateLock) {
            PluginRuntime runtime = plugins.get(pluginName);

            return runtime == null
                    ? null
                    : runtime.getPlugin();
        }
    }

    /**
     * 获取所有正常运行的插件。
     */
    public Map<String, PluginRuntime> getRuntime() {
        synchronized (stateLock) {
            return Collections.unmodifiableMap(
                    new LinkedHashMap<>(plugins)
            );
        }
    }

    /**
     * 判断插件是否处于 LOADED 状态。
     */
    public boolean isLoaded(String pluginName) {
        synchronized (stateLock) {
            PluginRuntime runtime = plugins.get(pluginName);

            return runtime != null
                    && runtime.getState().get() == PluginState.LOADED;
        }
    }

    /**
     * 获取所有失败的插件运行时。
     */
    public Map<String, PluginRuntime> getFailedPlugins() {
        synchronized (stateLock) {
            return Collections.unmodifiableMap(
                    new LinkedHashMap<>(failedPlugins)
            );
        }
    }

    /**
     * 获取指定插件的失败运行时。
     */
    public PluginRuntime getFailedPlugin(String pluginName) {
        synchronized (stateLock) {
            return failedPlugins.get(pluginName);
        }
    }

    /**
     * 判断指定插件是否存在失败记录。
     */
    public boolean isFailed(String pluginName) {
        synchronized (stateLock) {
            return failedPlugins.containsKey(pluginName);
        }
    }

    /**
     * 批量启动失败后的回滚。
     *
     * <p>按照加载顺序逆序清理。</p>
     */
    private void rollback(
            Map<String, PluginRuntime> runtimes,
            Set<String> lifecycleStarted,
            Throwable originalCause
    ) {
        List<PluginRuntime> reverseOrder =
                new ArrayList<>(runtimes.values());

        Collections.reverse(reverseOrder);

        for (PluginRuntime runtime : reverseOrder) {
            String pluginName = runtime.getName();

            boolean cleanupFailed = cleanupPartialRuntime(
                    runtime,
                    lifecycleStarted.contains(pluginName),
                    originalCause
            );

            if (cleanupFailed) {
                synchronized (stateLock) {
                    failedPlugins.put(pluginName, runtime);
                }
            }
        }
    }

    /**
     * 清理尚未完成正常加载的插件。
     *
     * <p>先关闭事件注册器，确保没有正在执行的事件回调，
     * 然后才允许执行 unload() 和关闭 ClassLoader。</p>
     *
     * @return 清理过程是否发生失败
     */
    private boolean cleanupPartialRuntime(
            PluginRuntime runtime,
            boolean lifecycleStarted,
            Throwable originalCause
    ) {
        boolean cleanupFailed = false;

        /*
         * 第一步：关闭事件注册器。
         *
         * 如果失败，不能执行 unload()，
         * 也不能关闭 ClassLoader。
         */
        boolean eventCleanupSucceeded = false;

        try {
            closeEventRegistry(runtime.getName());
            eventCleanupSucceeded = true;

        } catch (Throwable cleanupException) {
            cleanupFailed = true;

            addSuppressedSafely(
                    originalCause,
                    cleanupException
            );
        }

        if (!eventCleanupSucceeded) {
            runtime.markFailed();
            return true;
        }

        /*
         * 第二步：执行插件生命周期卸载回调。
         */
        if (lifecycleStarted) {
            try {
                invokeUnload(runtime);

            } catch (Throwable cleanupException) {
                cleanupFailed = true;

                addSuppressedSafely(
                        originalCause,
                        cleanupException
                );
            }
        }

        /*
         * 第三步：关闭 ClassLoader。
         *
         * 即使 unload() 抛出异常，
         * 由于事件监听器已经全部退出，仍然可以关闭 ClassLoader。
         */
        try {
            runtime.getClassLoader().close();

        } catch (Throwable cleanupException) {
            cleanupFailed = true;

            addSuppressedSafely(
                    originalCause,
                    cleanupException
            );
        }

        runtime.markFailed();

        return cleanupFailed;
    }

    /**
     * 关闭指定插件的事件注册器。
     *
     * <p>只有 close() 成功后才从注册表中移除引用。</p>
     */
    private void closeEventRegistry(String pluginName) {
        PluginEventRegistry registry;

        synchronized (stateLock) {
            registry = eventRegistries.get(pluginName);
        }

        if (registry == null) {
            return;
        }

        /*
         * 不能持有 stateLock 等待监听器结束。
         */
        registry.close();

        synchronized (stateLock) {
            eventRegistries.remove(
                    pluginName,
                    registry
            );
        }
    }

    /**
     * 正常卸载已经加载完成的插件。
     */
    private void unloadRuntime(PluginRuntime runtime) {
        Throwable failure = null;

        /*
         * 第一步：转换状态。
         */
        try {
            transition(
                    runtime,
                    PluginState.LOADED,
                    PluginState.UNLOADING
            );

        } catch (Throwable exception) {
            runtime.markFailed();

            rethrow(
                    exception,
                    "插件卸载状态转换失败: " + runtime.getName()
            );

            return;
        }

        /*
         * 第二步：关闭事件注册器。
         *
         * 在关闭成功并等待所有事件回调退出之前，
         * 不能调用插件 unload()。
         */
        boolean eventCleanupSucceeded = false;

        try {
            closeEventRegistry(runtime.getName());
            eventCleanupSucceeded = true;

        } catch (Throwable exception) {
            failure = exception;
        }

        if (!eventCleanupSucceeded) {
            runtime.markFailed();

            rethrow(
                    failure,
                    "插件事件注册器关闭失败: " + runtime.getName()
            );

            return;
        }

        /*
         * 第三步：执行插件 unload()。
         */
        try {
            runtime.getPlugin().unload();

        } catch (Throwable exception) {
            failure = exception;
        }

        /*
         * 第四步：关闭 ClassLoader。
         *
         * 事件注册器已经成功关闭，
         * 不会再有该插件的事件回调继续执行。
         */
        try {
            runtime.getClassLoader().close();

        } catch (Throwable exception) {
            if (failure == null) {
                failure = exception;
            } else {
                addSuppressedSafely(
                        failure,
                        exception
                );
            }
        }

        if (failure != null) {
            runtime.markFailed();

            rethrow(
                    failure,
                    "插件卸载失败: " + runtime.getName()
            );

            return;
        }

        /*
         * 第五步：全部成功后转换为 UNLOADED。
         */
        try {
            transition(
                    runtime,
                    PluginState.UNLOADING,
                    PluginState.UNLOADED
            );

        } catch (Throwable exception) {
            runtime.markFailed();

            rethrow(
                    exception,
                    "插件卸载状态转换失败: " + runtime.getName()
            );
        }
    }

    /**
     * 对部分生命周期状态执行 unload()。
     */
    private void invokeUnload(PluginRuntime runtime) {
        PluginState state = runtime.getState().get();

        switch (state) {
            case DISCOVERED, FAILED, UNLOADED -> {
                return;
            }

            case UNLOADING -> throw new IllegalStateException(
                    "插件已经处于卸载状态，拒绝重复执行 unload(): "
                            + runtime.getName()
            );

            case INITIALIZING,
                 INITIALIZED,
                 LOADING,
                 LOADED -> {
                if (!runtime.transition(
                        state,
                        PluginState.UNLOADING
                )) {
                    throw new IllegalStateException(
                            "插件状态转换失败: "
                                    + runtime.getName()
                                    + ", 当前状态: "
                                    + runtime.getState().get()
                    );
                }

                runtime.getPlugin().unload();
            }
        }
    }

    /**
     * 检查是否存在依赖当前插件的运行中插件。
     *
     * <p>除了正常运行插件，也检查失败运行时。
     * 对失败运行时采取保守策略：只要仍保留失败记录，
     * 就不能假定它已经完全释放依赖关系。</p>
     */
    private void checkDependents(
            String pluginName,
            Map<String, PluginRuntime> loadedPlugins,
            Map<String, PluginRuntime> failedSnapshot
    ) {
        for (PluginRuntime runtime : loadedPlugins.values()) {
            if (runtime.getName().equals(pluginName)) {
                continue;
            }

            if (runtime.getState().get() != PluginState.LOADED) {
                continue;
            }

            if (dependsOn(runtime, pluginName)) {
                throw new IllegalStateException(
                        "无法卸载插件 %s，插件 %s 仍然依赖它"
                                .formatted(
                                        pluginName,
                                        runtime.getName()
                                )
                );
            }
        }

        for (PluginRuntime runtime : failedSnapshot.values()) {
            if (runtime.getName().equals(pluginName)) {
                continue;
            }

            if (dependsOn(runtime, pluginName)) {
                throw new IllegalStateException(
                        "无法卸载插件 %s，失败插件运行时 %s 仍然声明依赖它。"
                                .formatted(
                                        pluginName,
                                        runtime.getName()
                                )
                );
            }
        }
    }

    /**
     * 判断插件是否声明依赖指定插件。
     */
    private boolean dependsOn(
            PluginRuntime runtime,
            String pluginName
    ) {
        PluginMeta meta = runtime.getMeta();

        List<String> depend = Objects.requireNonNullElse(
                meta.getDepend(),
                List.of()
        );

        List<String> softDepend = Objects.requireNonNullElse(
                meta.getSoftDepend(),
                List.of()
        );

        return depend.contains(pluginName)
                || softDepend.contains(pluginName);
    }

    /**
     * 禁止从事件监听器回调中同步执行生命周期操作。
     */
    private void ensureNotInEventCallback() {
        if (eventManager.isDispatchThread()) {
            throw new IllegalStateException(
                    "不允许在事件监听器回调中同步执行插件生命周期操作。"
                            + "请将操作提交到独立线程或任务队列中执行。"
            );
        }
    }

    /**
     * 执行严格的状态转换。
     */
    private void transition(
            PluginRuntime runtime,
            PluginState expected,
            PluginState next
    ) {
        if (!runtime.transition(expected, next)) {
            throw new IllegalStateException(
                    "插件状态转换失败: %s, 期望状态: %s, 实际状态: %s, 目标状态: %s"
                            .formatted(
                                    runtime.getName(),
                                    expected,
                                    runtime.getState().get(),
                                    next
                            )
            );
        }
    }

    /**
     * 添加 suppressed exception，避免异常对象自我抑制。
     */
    private void addSuppressedSafely(
            Throwable original,
            Throwable additional
    ) {
        if (original != additional) {
            original.addSuppressed(additional);
        }
    }

    /**
     * 保留原始异常类型并统一处理异常。
     */
    private void rethrow(
            Throwable cause,
            String message
    ) {
        if (cause instanceof Error error) {
            throw error;
        }

        if (cause instanceof RuntimeException exception) {
            throw exception;
        }

        throw new IllegalStateException(
                message,
                cause
        );
    }
}
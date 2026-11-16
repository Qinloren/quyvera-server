package com.zeeyeh.quyvera.plugin.service.plugin.loader;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 插件类加载器。AI生成
 * 有两个实现细节需要特别说明：
 * 1. SHARED_PACKAGES 是示例白名单。正式使用时应根据 SDK 允许的共享库制定，不能为了方便直接把 org.springframework.、com.zeeyeh. 全部放进去。
 * 2. 依赖插件必须先完成类加载器初始化。插件依赖关系不能形成循环，否则类加载过程可能出现递归和类型问题。
 * @author Qinloren
 * @date 2026/11/15 10:05
 */
public final class PluginClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    /**
     * 必须由父加载器加载的包。
     */
    private static final List<String> PARENT_ONLY_PACKAGES = List.of(
            "java.",
            "javax.crypto.",
            "javax.naming.",
            "javax.net.",
            "javax.security.",
            "javax.sql.",
            "javax.swing.",
            "javax.xml.",
            "jdk.",
            "sun.",
            "com.sun.",
            "org.w3c.dom.",
            "org.xml.sax.",
            "org.ietf.jgss.",

            "com.zeeyeh.quyvera.plugin.api.",
            "com.zeeyeh.quyvera.plugin.sdk."
    );

    /**
     * 父加载器优先的共享第三方包。
     */
    private static final List<String> PARENT_PREFERRED_PACKAGES = List.of(
            "org.slf4j.",
            "org.apache.logging.log4j."
    );

    /**
     * 插件可以访问的宿主公共资源目录。
     */
    private static final List<String> PARENT_VISIBLE_RESOURCES = List.of(
            "quyvera/shared/",
            "META-INF/services/"
    );

    /**
     * 只能由父加载器提供的资源目录。
     */
    private static final List<String> PARENT_ONLY_RESOURCES = List.of(
            "java/",
            "javax/crypto/",
            "javax/naming/",
            "javax/net/",
            "javax/security/",
            "javax/sql/",
            "javax/swing/",
            "javax/xml/",
            "jdk/",
            "sun/",
            "com/sun/",
            "org/w3c/dom/",
            "org/xml/sax/",
            "org/ietf/jgss/",

            "com/zeeyeh/quyvera/plugin/api/",
            "com/zeeyeh/quyvera/plugin/sdk/"
    );

    @Getter
    private final String pluginId;

    /**
     * 协调类加载、资源访问与关闭操作。
     *
     * 普通操作持有读锁。
     * close() 持有写锁。
     */
    private final ReentrantReadWriteLock lifecycleLock =
            new ReentrantReadWriteLock();

    /**
     * 插件声明的依赖加载器。
     */
    private volatile List<PluginClassLoader> dependencies =
            List.of();

    /**
     * 是否已经初始化依赖。
     */
    private boolean dependenciesInitialized;

    /**
     * 是否已经进入关闭状态。
     *
     * 一旦设置为 true，就不允许再进行类加载和资源访问。
     *
     * 即使关闭失败，也不会重新设置为 false。
     */
    @Getter
    private volatile boolean closed;

    /**
     * URLClassLoader 底层资源是否已经成功关闭。
     *
     * 与 closed 分离，允许关闭失败后重试。
     */
    private boolean urlLoaderClosed;

    /**
     * 是否已经完成全部关闭操作。
     */
    private boolean closeCompleted;

    /**
     * 当前加载器创建的资源流。
     */
    private final Set<TrackedInputStream> openedStreams =
            ConcurrentHashMap.newKeySet();

    public PluginClassLoader(
            String pluginId,
            URL[] urls,
            ClassLoader parent
    ) {

        super(
                Objects.requireNonNull(urls, "urls").clone(),
                Objects.requireNonNull(
                        parent,
                        "Plugin parent ClassLoader cannot be null"
                )
        );

        if (pluginId == null || pluginId.isBlank()) {
            throw new IllegalArgumentException(
                    "Plugin ID cannot be empty"
            );
        }

        this.pluginId = pluginId;
    }

    // =====================================================
    // Dependency Initialization
    // =====================================================

    /**
     * 初始化插件依赖。
     *
     * 必须在加载插件主类之前调用。
     *
     * 依赖顺序由 PluginDependencyManager 决定。
     */
    public void initializeDependencies(
            List<PluginClassLoader> dependencies
    ) {

        lifecycleLock.writeLock().lock();

        try {

            ensureOpen();

            if (dependenciesInitialized) {
                throw new IllegalStateException(
                        "Dependencies already initialized: " + pluginId
                );
            }

            Objects.requireNonNull(
                    dependencies,
                    "dependencies"
            );

            List<PluginClassLoader> copy =
                    List.copyOf(dependencies);

            Set<PluginClassLoader> unique =
                    Collections.newSetFromMap(
                            new IdentityHashMap<>()
                    );

            for (PluginClassLoader dependency : copy) {

                Objects.requireNonNull(
                        dependency,
                        "dependency"
                );

                if (dependency == this) {
                    throw new IllegalArgumentException(
                            "Plugin cannot depend on itself: " + pluginId
                    );
                }

                if (!unique.add(dependency)) {
                    throw new IllegalArgumentException(
                            "Duplicate dependency ClassLoader: "
                                    + dependency.pluginId
                    );
                }

                if (dependency.isClosed()) {
                    throw new IllegalStateException(
                            "Dependency ClassLoader is closed: "
                                    + dependency.pluginId
                    );
                }
            }

            this.dependencies = copy;
            this.dependenciesInitialized = true;

        } finally {

            lifecycleLock.writeLock().unlock();
        }
    }

    // =====================================================
    // Class Loading
    // =====================================================

    @Override
    protected Class<?> loadClass(
            String name,
            boolean resolve
    ) throws ClassNotFoundException {

        Objects.requireNonNull(name, "class name");

        lifecycleLock.readLock().lock();

        try {

            ensureOpen();

            synchronized (getClassLoadingLock(name)) {

                Class<?> loadedClass = findLoadedClass(name);

                if (loadedClass != null) {

                    if (resolve
                            && loadedClass.getClassLoader() == this) {
                        resolveClass(loadedClass);
                    }

                    return loadedClass;
                }

                Class<?> result;

                // 1. JDK、SDK、API：只能由父加载器加载。
                if (isParentOnlyClass(name)) {

                    result = getParent().loadClass(name);

                }
                // 2. 共享第三方包：父加载器优先。
                else if (isParentPreferredClass(name)) {

                    result = loadParentPreferred(name);

                }
                // 3. 普通类：插件自身优先。
                else {

                    result = loadChildFirst(name);
                }

                if (resolve && result.getClassLoader() == this) {
                    resolveClass(result);
                }

                return result;
            }

        } finally {

            lifecycleLock.readLock().unlock();
        }
    }

    /**
     * Parent-First。
     *
     * 父加载器找不到时，尝试插件自身及声明的依赖。
     */
    private Class<?> loadParentPreferred(
            String name
    ) throws ClassNotFoundException {

        try {

            return getParent().loadClass(name);

        } catch (ClassNotFoundException ignored) {

            return loadChildFirst(name);
        }
    }

    /**
     * Child-First。
     *
     * 查找顺序：
     * 1. 当前插件。
     * 2. 声明的依赖插件。
     *
     * 不允许任意回退到宿主 ClassLoader。
     */
    private Class<?> loadChildFirst(
            String name
    ) throws ClassNotFoundException {

        try {

            return findClass(name);

        } catch (ClassNotFoundException ignored) {
            // 当前插件没有该类，继续查找依赖。
        }

        for (PluginClassLoader dependency : dependencies) {

            try {

                return dependency.loadClass(name);

            } catch (ClassNotFoundException ignored) {
                // 继续查找下一个依赖。
            }
        }

        throw new ClassNotFoundException(
                "Class not found in plugin "
                        + pluginId + ": " + name
        );
    }

    private boolean isParentOnlyClass(String className) {

        return PARENT_ONLY_PACKAGES.stream()
                .anyMatch(className::startsWith);
    }

    private boolean isParentPreferredClass(String className) {

        return PARENT_PREFERRED_PACKAGES.stream()
                .anyMatch(className::startsWith);
    }

    // =====================================================
    // Resource Loading
    // =====================================================

    @Override
    public URL getResource(String name) {

        String resourceName = normalizeResourceName(name);

        lifecycleLock.readLock().lock();

        try {

            ensureOpen();

            // 受保护资源只能从父加载器获取。
            if (isParentOnlyResource(resourceName)) {
                return getParent().getResource(resourceName);
            }

            // 1. 当前插件。
            URL localResource = findResource(resourceName);

            if (localResource != null) {
                return localResource;
            }

            // 2. 声明的依赖插件。
            for (PluginClassLoader dependency : dependencies) {

                URL resource = dependency.getResource(resourceName);

                if (resource != null) {
                    return resource;
                }
            }

            // 3. 宿主公共资源白名单。
            if (isParentVisibleResource(resourceName)) {
                return getParent().getResource(resourceName);
            }

            return null;

        } finally {

            lifecycleLock.readLock().unlock();
        }
    }

    @Override
    public Enumeration<URL> getResources(
            String name
    ) throws IOException {

        String resourceName = normalizeResourceName(name);

        lifecycleLock.readLock().lock();

        try {

            ensureOpen();

            Map<String, URL> result = new LinkedHashMap<>();

            // 受保护资源只能从宿主获取。
            if (isParentOnlyResource(resourceName)) {

                addResources(
                        result,
                        getParent().getResources(resourceName)
                );

                return Collections.enumeration(
                        result.values()
                );
            }

            // 1. 当前插件。
            addResources(
                    result,
                    findResources(resourceName)
            );

            // 2. 声明的依赖插件。
            for (PluginClassLoader dependency : dependencies) {

                addResources(
                        result,
                        dependency.getResources(resourceName)
                );
            }

            // 3. 宿主公共资源白名单。
            if (isParentVisibleResource(resourceName)) {

                addResources(
                        result,
                        getParent().getResources(resourceName)
                );
            }

            return Collections.enumeration(
                    result.values()
            );

        } finally {

            lifecycleLock.readLock().unlock();
        }
    }

    /**
     * 获取资源流。
     *
     * 资源流会被实际打开它的 ClassLoader 跟踪。
     *
     * 如果资源来自依赖插件，则依赖插件和当前插件
     * 都会持有对应的关闭责任。
     */
    @Override
    public InputStream getResourceAsStream(String name) {

        String resourceName = normalizeResourceName(name);

        return openResourceAsStream(resourceName);
    }

    /**
     * 在当前加载器的读锁保护下查找并打开资源。
     */
    private InputStream openResourceAsStream(
            String resourceName
    ) {

        lifecycleLock.readLock().lock();

        try {

            ensureOpen();

            // 1. 受保护资源只能从父加载器获取。
            if (isParentOnlyResource(resourceName)) {

                return openAndTrack(
                        getParent().getResource(resourceName)
                );
            }

            // 2. 当前插件。
            URL localResource = findResource(resourceName);

            if (localResource != null) {

                return openAndTrack(localResource);
            }

            // 3. 声明的依赖插件。
            for (PluginClassLoader dependency : dependencies) {

                InputStream stream =
                        dependency.openResourceAsStream(resourceName);

                if (stream != null) {

                    return trackStream(stream);
                }
            }

            // 4. 宿主公共资源白名单。
            if (isParentVisibleResource(resourceName)) {

                return openAndTrack(
                        getParent().getResource(resourceName)
                );
            }

            return null;

        } finally {

            lifecycleLock.readLock().unlock();
        }
    }

    /**
     * 打开 URL 并跟踪资源流。
     */
    private InputStream openAndTrack(URL resource) {

        if (resource == null) {
            return null;
        }

        try {

            return trackStream(resource.openStream());

        } catch (IOException exception) {

            return null;
        }
    }

    /**
     * 将已有资源流纳入当前加载器的生命周期管理。
     */
    private InputStream trackStream(InputStream delegate) {

        TrackedInputStream tracked =
                new TrackedInputStream(
                        delegate,
                        openedStreams
                );

        openedStreams.add(tracked);

        return tracked;
    }

    private boolean isParentOnlyResource(String resourceName) {

        return PARENT_ONLY_RESOURCES.stream()
                .anyMatch(resourceName::startsWith);
    }

    private boolean isParentVisibleResource(String resourceName) {

        return PARENT_VISIBLE_RESOURCES.stream()
                .anyMatch(resourceName::startsWith);
    }

    /**
     * 规范化资源路径。
     *
     * 不允许路径穿越、反斜杠、NUL 字符及非法路径段。
     */
    private static String normalizeResourceName(String name) {

        Objects.requireNonNull(name, "resource name");

        String normalized = name;

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.isBlank()
                || normalized.contains("\\")
                || normalized.indexOf('\0') >= 0) {

            throw new IllegalArgumentException(
                    "Invalid resource name: " + name
            );
        }

        String[] segments = normalized.split("/", -1);

        for (String segment : segments) {

            if (segment.equals("..")
                    || segment.equals(".")) {

                throw new IllegalArgumentException(
                        "Invalid resource path segment: " + name
                );
            }
        }

        return normalized;
    }

    /**
     * 将 Enumeration 中的资源加入结果集。
     */
    private static void addResources(
            Map<String, URL> target,
            Enumeration<URL> resources
    ) {

        while (resources.hasMoreElements()) {

            URL url = resources.nextElement();

            target.putIfAbsent(
                    url.toExternalForm(),
                    url
            );
        }
    }

    // =====================================================
    // Lifecycle
    // =====================================================

    private void ensureOpen() {

        if (closed) {

            throw new IllegalStateException(
                    "PluginClassLoader is closed: " + pluginId
            );
        }
    }

    /**
     * 关闭插件 ClassLoader。
     *
     * <p>关闭失败后允许再次调用 close() 重试。</p>
     *
     * <p>一旦进入关闭状态，就不会重新开放类加载和资源访问。</p>
     */
    @Override
    public void close() throws IOException {

        lifecycleLock.writeLock().lock();

        try {

            if (closeCompleted) {
                return;
            }

            // 第一次进入关闭流程时，永久禁止后续访问。
            closed = true;

            // 解除对其他插件 ClassLoader 的引用。
            dependencies = List.of();

            IOException firstException = null;

            // 关闭所有跟踪的资源流。
            for (TrackedInputStream stream : openedStreams) {

                try {

                    stream.close();

                } catch (IOException exception) {

                    firstException = mergeException(
                            firstException,
                            exception
                    );
                }
            }

            // 只有全部资源流关闭成功后才关闭 URLClassLoader。
            if (openedStreams.isEmpty() && !urlLoaderClosed) {

                try {

                    super.close();

                    urlLoaderClosed = true;

                } catch (IOException exception) {

                    firstException = mergeException(
                            firstException,
                            exception
                    );
                }
            }

            // 只有所有资源都成功关闭，才算完成。
            if (openedStreams.isEmpty() && urlLoaderClosed) {

                closeCompleted = true;
            }

            if (firstException != null) {
                throw firstException;
            }

            if (!closeCompleted) {

                throw new IOException(
                        "PluginClassLoader close was not completed: "
                                + pluginId
                );
            }

        } finally {

            lifecycleLock.writeLock().unlock();
        }
    }

    /**
     * 合并关闭异常。
     */
    private static IOException mergeException(
            IOException first,
            IOException next
    ) {

        if (first == null) {
            return next;
        }

        first.addSuppressed(next);

        return first;
    }

    // =====================================================
    // Tracked InputStream
    // =====================================================

    /**
     * 被当前 ClassLoader 跟踪的资源流。
     *
     * 关闭失败时不会从注册表移除，
     * 允许后续 close() 重试。
     */
    private static final class TrackedInputStream
            extends InputStream {

        private final InputStream delegate;

        private final Set<TrackedInputStream> registry;

        private boolean streamClosed;

        private TrackedInputStream(
                InputStream delegate,
                Set<TrackedInputStream> registry
        ) {

            this.delegate = Objects.requireNonNull(
                    delegate,
                    "delegate"
            );

            this.registry = Objects.requireNonNull(
                    registry,
                    "registry"
            );
        }

        private void ensureStreamOpen() throws IOException {

            if (streamClosed) {

                throw new IOException(
                        "Stream is closed"
                );
            }
        }

        @Override
        public synchronized int read() throws IOException {

            ensureStreamOpen();

            return delegate.read();
        }

        @Override
        public synchronized int read(
                byte @NonNull [] bytes,
                int offset,
                int length
        ) throws IOException {

            ensureStreamOpen();

            Objects.checkFromIndexSize(
                    offset,
                    length,
                    bytes.length
            );

            return delegate.read(
                    bytes,
                    offset,
                    length
            );
        }

        @Override
        public synchronized long skip(
                long n
        ) throws IOException {

            ensureStreamOpen();

            return delegate.skip(n);
        }

        @Override
        public synchronized int available() throws IOException {

            ensureStreamOpen();

            return delegate.available();
        }

        @Override
        public synchronized void close() throws IOException {

            if (streamClosed) {
                return;
            }

            // 先尝试关闭底层资源。
            // 只有成功后才标记关闭并移除注册。
            delegate.close();

            streamClosed = true;

            registry.remove(this);
        }
    }
}
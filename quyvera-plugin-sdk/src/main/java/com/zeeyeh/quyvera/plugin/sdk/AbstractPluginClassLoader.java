package com.zeeyeh.quyvera.plugin.sdk;

import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import com.zeeyeh.quyvera.plugin.sdk.exceptions.InvalidPluginException;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 插件类加载器
 * @author Qinloren
 * @date 2026/10/25 21:10
 */
@Getter
public class AbstractPluginClassLoader extends URLClassLoader {
    private final PluginMeta pluginMeta;
    private final File pluginFile;
    private final JarFile pluginJarFile;
    private final URL pluginUrl;

    private final Set<Class<?>> definedClasses = ConcurrentHashMap.newKeySet();

    static {
        registerAsParallelCapable();
    }

    public AbstractPluginClassLoader(ClassLoader parent, PluginMeta pluginMeta, File pluginFile) throws IOException {
        super(pluginMeta.getName(), new URL[] {pluginFile.toURI().toURL()}, parent);
        this.pluginMeta = pluginMeta;
        this.pluginFile = pluginFile;
        this.pluginJarFile = new JarFile(pluginFile);
        this.pluginUrl = pluginFile.toURI().toURL();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        JarEntry entry = pluginJarFile.getJarEntry(path);
        if (entry == null) {
            throw new ClassNotFoundException(name);
        }
        byte[] bytes;
        try (InputStream inputStream = pluginJarFile.getInputStream(entry)) {
            bytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new ClassNotFoundException("读取插件 [" + pluginMeta.getName() + "] 类文件失败", e);
        }
        definePackageIfNeeded(name);
        CodeSource source = new CodeSource(pluginUrl, entry.getCodeSigners());
        Class<?> clazz = defineClass(name, bytes, 0, bytes.length, source);
        definedClasses.add(clazz);
        return clazz;
    }

    /**
     * 定义包信息，如果包不存在则抛出异常
     * @param className 类名
     */
    private void definePackageIfNeeded(String className) {
        int dot = className.lastIndexOf('.');
        if (dot <= 0) return;
        String packageName = className.substring(0, dot);
        if (getDefinedPackage(packageName) != null) return;
        try {
            Manifest manifest = pluginJarFile.getManifest();
            if (manifest != null) {
                definePackage(packageName, manifest, pluginUrl);
            } else {
                definePackage(packageName, null, null, null, null, null, null, null);
            }
        } catch (IllegalArgumentException | IOException e) {
            if (getDefinedPackage(packageName) == null) {
                throw new IllegalStateException("无法定义包 [" + packageName + "]", e);
            }
        }
    }

    /**
     * 加载主类并检查继承关系
     * @return 主类实例
     */
    public Class<?> loadMainClass() {
        String main = pluginMeta.getMain();
        try {
            return Class.forName(main, true, this);
        } catch (ClassNotFoundException e) {
            throw new InvalidPluginException("插件 [" + pluginMeta.getName() + "] 主类不存在", e);
        }
    }

    /**
     * 加载主类并检查继承关系
     * @param baseType 主类基础类型
     * @return 主类实例
     * @param <T> 主类基础类型
     */
    public <T> Class<? extends T> loadMainClass(Class<T> baseType) {
        Class<?> mainClass = loadMainClass();
        try {
            return mainClass.asSubclass(baseType);
        } catch (ClassCastException e) {
            throw new InvalidPluginException("插件 [" + pluginMeta.getName() + "] 主类 [" + mainClass.getName() + "] 必须继承 " + baseType.getName(), e);
        }
    }

    /**
     * 加载主类并实例化
     * @param baseType 主类基础类型
     * @return 主类实例
     * @param <T> 主类基础类型
     */
    public <T> T instantiateMainClass(Class<T> baseType) {
        Class<? extends T> mainClass = loadMainClass(baseType);
        try {
            Constructor<? extends T> constructor = mainClass.getDeclaredConstructor();
            if (!Modifier.isPublic(constructor.getModifiers())) {
                throw new InvalidPluginException("无法实例化主类 [" + mainClass.getName() + "] 无参构造函数必须为 public");
            }
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new InvalidPluginException("插件 [" + pluginMeta.getName() + "] 主类 [" + mainClass.getName() + "] 必须存在无参构造函数", e);
        } catch (InstantiationException e) {
            throw new InvalidPluginException("插件 [" + pluginMeta.getName() + "] 主类 [" + mainClass.getName() + "] 不能是抽象类或接口", e);
        } catch (IllegalAccessException e) {
            throw new InvalidPluginException("插件 [" + pluginMeta.getName() + "] 主类 [" + mainClass.getName() + "] 构造方法无权访问", e);
        } catch (InvocationTargetException e) {
            throw new InvalidPluginException("插件 [" + pluginMeta.getName() + "] 主类 [" + mainClass.getName() + "] 构造方法抛出异常", e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            try {
                pluginJarFile.close();
            } finally {
                definedClasses.clear();
            }
        }
    }

    /**
     * 获取定义的类集合
     * @return 定义的类集合
     */
    public Set<Class<?>> getDefinedClasses() {
        return Collections.unmodifiableSet(definedClasses);
    }
}

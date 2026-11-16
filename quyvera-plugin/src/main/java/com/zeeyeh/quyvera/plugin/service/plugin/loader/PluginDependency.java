package com.zeeyeh.quyvera.plugin.service.plugin.loader;

import com.zeeyeh.quyvera.plugin.sdk.exceptions.InvalidPluginException;

import java.util.Objects;
import java.util.Set;

/**
 * 插件依赖
 * @author Qinloren
 * @date 2026/11/15 10:03
 */
public record PluginDependency(
        PluginClassLoader classLoader,
        Set<String> exportedPackages
) {

    public PluginDependency {
        Objects.requireNonNull(classLoader, "classLoader");
        exportedPackages = Set.copyOf(Objects.requireNonNull(exportedPackages, "exportedPackages"));
        exportedPackages.forEach(PluginDependency::validatePackage);
    }

    public boolean exportsClass(String className) {
        return exportedPackages.stream()
                .anyMatch(packageName -> isInPackage(className, packageName));
    }

    public boolean exportsResource(String resourceName) {
        String normalized = resourceName.startsWith("/") ? resourceName.substring(1) : resourceName;
        return exportedPackages.stream()
                .map(name -> name.replace(".", "/"))
                .anyMatch(packagePath -> normalized.equals(packagePath) || normalized.startsWith(packagePath + "/"));
    }

    private static boolean isInPackage(String className, String packageName) {
        return className.startsWith(packageName + ".");
    }

    private static void validatePackage(String packageName) {
        Objects.requireNonNull(packageName);
        if (packageName.isBlank()|| packageName.startsWith(".") || packageName.endsWith(".") || packageName.contains("..")) {
            throw new InvalidPluginException("非法包路径:" + packageName);
        }
    }
}

package com.zeeyeh.quyvera.plugin.sdk.entity;

import com.zeeyeh.quyvera.common.core.utils.BusinessAsserts;
import com.zeeyeh.quyvera.plugin.sdk.enums.PluginErrorCode;
import com.zeeyeh.quyvera.plugin.sdk.exceptions.InvalidPluginException;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 语义化版本号工具类
 * @author Qinloren
 * @date 2026/10/25 19:05
 */
@Getter
public class SemanticVersion implements Comparable<SemanticVersion>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private final int major;
    private final int minor;
    private final int patch;
    private final List<String> preRelease;
    private final List<String> buildMetadata;

    private SemanticVersion(int major, int minor, int patch, List<String> preRelease, List<String> buildMetadata) {
        BusinessAsserts.check(major < 0 || minor < 0 || patch < 0, PluginErrorCode.VERSION_NOT_LESS_THAN_ZERO);
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preRelease = List.copyOf(preRelease);
        this.buildMetadata = List.copyOf(buildMetadata);
    }

    /**
     * 创建版本对象
     * @param major 主版本号
     * @param minor 次版本号
     * @param patch 补丁号
     * @return 版本对象
     */
    public static SemanticVersion of(int major, int minor, int patch) {
        return new SemanticVersion(major, minor, patch, List.of(), List.of());
    }

    /**
     * 创建版本对象
     * @param major 主版本号
     * @param minor 次版本号
     * @param patch 补丁号
     * @param preRelease 预发布标识符
     * @param buildMetadata 构建元数据
     * @return 版本对象
     */
    public static SemanticVersion of(int major, int minor, int patch, String preRelease, String buildMetadata) {
        StringBuilder builder = new StringBuilder();
        builder.append(major)
                .append(".")
                .append(minor)
                .append(".")
                .append(patch);
        if (StringUtils.isNotEmpty(preRelease)) {
            builder.append("-")
                    .append(preRelease);
        }
        if (StringUtils.isNotEmpty(buildMetadata)) {
            builder.append("+")
                    .append(buildMetadata);
        }
        return parse(builder.toString());
    }

    /**
     * 解析版本字符串
     * @param version 版本字符串
     * @return 版本对象；入参为 null 或空白时返回 null
     */
    public static SemanticVersion parse(String version) {
        if (StringUtils.isBlank(version)) {
            return null;
        }
        String remaining = version.trim();

        // 先剥离构建元数据（+ 之后），只在核心段上再找预发布分隔符，
        // 否则 "1.0.0+build-1" 会把 '-' 的查找位置落在构建元数据里导致越界。
        String core = remaining;
        String build = null;
        int plusIndex = remaining.indexOf('+');
        if (plusIndex >= 0) {
            core = remaining.substring(0, plusIndex);
            build = remaining.substring(plusIndex + 1);
            BusinessAsserts.check(build.indexOf('+') >= 0, PluginErrorCode.VERSION_BUILD_ILLEGAL);
        }

        String mainPart = core;
        String preReleasePart = null;
        int dashIndex = core.indexOf('-');
        if (dashIndex >= 0) {
            mainPart = core.substring(0, dashIndex);
            preReleasePart = core.substring(dashIndex + 1);
        }

        String[] parts = StringUtils.split(mainPart, "\\.", -1);
        BusinessAsserts.check(parts.length != 3, PluginErrorCode.VERSION_ILLEGAL);
        int major;
        int minor;
        int patch;
        try {
            major = Integer.parseInt(parts[0]);
            minor = Integer.parseInt(parts[1]);
            patch = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new InvalidPluginException("非法版本段: " + mainPart, e);
        }
        List<String> preRelease = preReleasePart == null ? List.of() : parseIdentifiers(preReleasePart, version);
        List<String> buildMetadata = build == null ? List.of() : parseIdentifiers(build, version);
        return new SemanticVersion(major, minor, patch, preRelease, buildMetadata);
    }

    /**
     * 解析标识符
     * @param raw 标识符字符串
     * @param origin 原始版本字符串
     * @return 标识符列表
     */
    private static List<String> parseIdentifiers(String raw, String origin) {
        String[] parts = StringUtils.split(raw, "\\.", -1);
        for (String part : parts) {
            BusinessAsserts.check(StringUtils.isEmpty(part), PluginErrorCode.VERSION_ILLEGAL);
            for (int i = 0; i < part.length(); i++) {
                char c = part.charAt(i);
                boolean legal = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '-';
                // BusinessAsserts.check 的语义是「条件成立即视为非法并抛出」，所以这里必须传入否定式，
                // 否则合法字符反而会触发断言（原实现即因此使所有带 - 或 + 的版本号解析失败）。
                BusinessAsserts.check(!legal, PluginErrorCode.VERSION_ILLEGAL);
            }
        }
        return List.of(parts);
    }

    @Override
    public int compareTo(@NonNull SemanticVersion o) {
        if (this == o) {
            return 0;
        }
        int compared = Integer.compare(this.major, o.major);
        if (compared != 0) {
            return compared;
        }
        compared = Integer.compare(this.minor, o.minor);
        if (compared != 0) {
            return compared;
        }
        compared = Integer.compare(this.patch, o.patch);
        if (compared != 0) {
            return compared;
        }
        return comparePreRelease(this.preRelease, o.preRelease);
    }

    /**
     * 比较两个版本号的预发布部分
     * @param prev 前版本号的预发布部分
     * @param next 后版本号的预发布部分
     * @return 比较结果
     */
    private static int comparePreRelease(List<String> prev, List<String> next) {
        if (prev.isEmpty() && next.isEmpty()) {
            return 0;
        }
        // 稳定版优先级高于同核心号的预发布版
        if (prev.isEmpty()) {
            return 1;
        }
        if (next.isEmpty()) {
            return -1;
        }
        int size = Math.min(prev.size(), next.size());
        for (int i = 0; i < size; i++) {
            int compared = compareIdentifier(prev.get(i), next.get(i));
            if (compared != 0) {
                return compared;
            }
        }
        return Integer.compare(prev.size(), next.size());
    }

    /**
     * 比较两个标识符
     * @param prev 前标识符
     * @param next 后标识符
     * @return 比较结果
     */
    private static int compareIdentifier(String prev, String next) {
        boolean prevNumeric = StringUtils.isNumeric(prev);
        boolean nextNumeric = StringUtils.isNumeric(next);
        if (prevNumeric && nextNumeric) {
            // 纯数字标识符按数值比较：位数不同时位数多者更大，位数相同按字典序即等价于数值序
            if (prev.length() != next.length()) {
                return Integer.compare(prev.length(), next.length());
            }
            return prev.compareTo(next);
        }
        // 数字标识符优先级低于字母数字标识符
        if (prevNumeric) {
            return -1;
        }
        if (nextNumeric) {
            return 1;
        }
        return prev.compareTo(next);
    }

    /**
     * 是否是预发布版本
     * @return 是否是预发布版本
     */
    public boolean isPreRelease() {
        return !preRelease.isEmpty();
    }

    /**
     * 是否是稳定版本
     * @return 是否是稳定版本
     */
    public boolean isStable() {
        return preRelease.isEmpty();
    }

    /**
     * 下一个主版本
     * @return 下一个主版本
     */
    public SemanticVersion nextMajor() {
        return new SemanticVersion(major + 1, 0, 0, List.of(), List.of());
    }

    /**
     * 下一个次版本
     * @return 下一个次版本
     */
    public SemanticVersion nextMinor() {
        return new SemanticVersion(major, minor + 1, 0, List.of(), List.of());
    }

    /**
     * 下一个补丁版本
     * @return 下一个补丁版本
     */
    public SemanticVersion nextPatch() {
        return new SemanticVersion(major, minor, patch + 1, List.of(), List.of());
    }

    /**
     * 返回当前版本与 {@code version} 中较大的那一个
     * @param version 版本对象
     * @return 较大版本
     */
    public SemanticVersion max(SemanticVersion version) {
        return compareTo(version) >= 0 ? this : version;
    }

    /**
     * 返回当前版本与 {@code version} 中较小的那一个
     * @param version 版本对象
     * @return 较小版本
     */
    public SemanticVersion min(SemanticVersion version) {
        return compareTo(version) <= 0 ? this : version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SemanticVersion that)) {
            return false;
        }
        return major == that.major
                && minor == that.minor
                && patch == that.patch
                && preRelease.equals(that.preRelease)
                && buildMetadata.equals(that.buildMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, preRelease, buildMetadata);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(major)
                .append('.')
                .append(minor)
                .append('.')
                .append(patch);
        if (!preRelease.isEmpty()) {
            builder.append('-')
                    .append(StringUtils.join(preRelease, '.'));
        }
        if (!buildMetadata.isEmpty()) {
            builder.append('+')
                    .append(StringUtils.join(buildMetadata, '.'));
        }
        return builder.toString();
    }
}

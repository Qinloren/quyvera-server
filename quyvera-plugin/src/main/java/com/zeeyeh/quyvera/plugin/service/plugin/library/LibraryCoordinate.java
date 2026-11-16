package com.zeeyeh.quyvera.plugin.service.plugin.library;


/**
 * 依赖坐标。AI生成
 * @author Qinloren
 * @date 2026/11/15 12:53
 */
public record LibraryCoordinate(
        String groupId,
        String artifactId,
        String version
) {

    public LibraryCoordinate {
        validate(groupId, "groupId");
        validate(artifactId, "artifactId");
        validate(version, "version");
        if (groupId.contains("/") || groupId.contains("\\")) {
            throw new IllegalArgumentException("Invalid groupId: %s".formatted(groupId));
        }
        if (artifactId.contains("/") || artifactId.contains("\\")) {
            throw new IllegalArgumentException("Invalid artifactId: %s".formatted(artifactId));
        }
        if (version.contains("/") || version.contains("\\")) {
            throw new IllegalArgumentException("Invalid version: %s".formatted(version));
        }
        if (groupId.startsWith(".") || groupId.endsWith(".") || groupId.contains("..")) {
            throw new IllegalArgumentException("Invalid groupId: %s".formatted(groupId));
        }
    }

    private static void validate(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("%s cannot be empty".formatted(field));
        }
        if (!value.equals(value.trim())) {
            throw new IllegalArgumentException("%s cannot contain surrounding whitespace".formatted(field));
        }
        if (value.contains(":")) {
            throw new IllegalArgumentException("Invalid %s: %s".formatted(field, value));
        }
    }

    public static LibraryCoordinate parse(String coordinate) {
        if (coordinate == null || coordinate.isBlank()) {
            throw new IllegalArgumentException("Library coordinate cannot be empty");
        }
        String[] parts = coordinate.split(":", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Maven coordinate: %s. Expected groupId:artifactId:version".formatted(coordinate));
        }
        return new LibraryCoordinate(parts[0], parts[1], parts[2]);
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
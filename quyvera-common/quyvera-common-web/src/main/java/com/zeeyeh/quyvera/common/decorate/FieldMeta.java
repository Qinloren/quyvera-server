package com.zeeyeh.quyvera.common.decorate;

import tools.jackson.databind.JsonNode;

/**
 * 字段元信息
 * @author Qinloren
 * @param name 字段名
 * @param path 字段路径
 * @param value 字段值
 */
public record FieldMeta(
        String name,
        String path,
        JsonNode value
) {
}

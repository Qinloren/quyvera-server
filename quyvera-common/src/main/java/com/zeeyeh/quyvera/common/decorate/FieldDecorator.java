package com.zeeyeh.quyvera.common.decorate;

import tools.jackson.databind.JsonNode;

public interface FieldDecorator {
    boolean applies(FieldMeta meta);

    JsonNode decorate(FieldMeta fieldMeta, JsonNode value, DecoratorContext context);
}

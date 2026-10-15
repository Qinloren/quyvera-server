package com.zeeyeh.quyvera.common.decorate;

import lombok.Getter;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DecoratorContext {
    @Getter
    private final JsonNodeFactory nodeFactory;
    private final Map<String, Object> shared = new HashMap<>();
    private boolean touched;

    public DecoratorContext(JsonNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    public Object get(String key) {
        return shared.get(key);
    }

    public void put(String key, Object value) {
        shared.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T computeIfAbsent(String key, Function<String, T> loader) {
        return (T) shared.computeIfAbsent(key, loader);
    }

    void markTouched() {
        this.touched = true;
    }

    boolean isTouched() {
        return touched;
    }
}

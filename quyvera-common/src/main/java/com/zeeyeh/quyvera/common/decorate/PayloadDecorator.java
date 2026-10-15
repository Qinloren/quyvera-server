package com.zeeyeh.quyvera.common.decorate;

import lombok.AllArgsConstructor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class PayloadDecorator {

    private final JsonMapper jsonMapper;
    private final List<FieldDecorator> decorators;
    private static final List<Class<?>> IGNORED_CONVERTERS = List.of(
            ByteArrayHttpMessageConverter.class,
            StringHttpMessageConverter.class,
            ResourceHttpMessageConverter.class
    );

    public JsonNode decorate(Object body) {
        if (body == null || decorators.isEmpty()) {
            return null;
        }
        JsonNode root = jsonMapper.valueToTree(body);
        if (!root.isContainer()) {
            return null;
        }
        DecoratorContext context = new DecoratorContext(JsonNodeFactory.instance);
        walk(root, "", context);
        return context.isTouched() ? root : null;
    }

    public boolean supports(Class<?> converterType) {
        return !decorators.isEmpty() && IGNORED_CONVERTERS.stream().noneMatch(ignored -> ignored.isAssignableFrom(converterType));
    }

    private void walk(JsonNode node, String path, DecoratorContext context) {
        if (node.isObject()) {
            walkObject((ObjectNode) node, path, context);
        } else if (node.isArray()) {
            walkArray((ArrayNode) node, path, context);
        }
    }

    private void walkObject(ObjectNode object, String path, DecoratorContext context) {
        List<Map.Entry<String, JsonNode>> pending = new ArrayList<>();
        Set<Map.Entry<String, JsonNode>> properties = object.properties();
        for (Map.Entry<String, JsonNode> property : properties) {
            String name = property.getKey();
            String fullPath = path.isEmpty() ? name : path + "." + name;
            FieldMeta meta = new FieldMeta(name, fullPath, property.getValue());
            JsonNode replacement = applyDecorators(meta, context);
            if (replacement != null) {
                pending.add(Map.entry(name, replacement));
                context.markTouched();;
                walk(replacement, fullPath, context);
            } else {
                walk(property.getValue(), fullPath, context);
            }
        }
        for (Map.Entry<String, JsonNode> entry : pending) {
            object.set(entry.getKey(), entry.getValue());
        }
    }

    private void walkArray(ArrayNode arrayNode, String path, DecoratorContext context) {
        for (int i = 0; i < arrayNode.size(); i++) {
            walk(arrayNode.get(i), path + "[" + i + "]", context);
        }
    }

    private JsonNode applyDecorators(FieldMeta meta, DecoratorContext context) {
        JsonNode current = meta.value();
        for (FieldDecorator decorator : decorators) {
            if (!decorator.applies(meta)) {
                continue;
            }
            JsonNode next = decorator.decorate(meta, current, context);
            if (next != null) {
                current = next;
            }
        }
        return current == meta.value() ? null : current;
    }
}

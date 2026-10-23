package com.zeeyeh.quyvera.user.decorate;

import com.zeeyeh.quyvera.common.decorate.DecoratorContext;
import com.zeeyeh.quyvera.common.decorate.FieldDecorator;
import com.zeeyeh.quyvera.common.decorate.FieldMeta;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.regex.Pattern;

/**
 * 手机号掩码装饰器
 * @author Qinloren
 * @date 2026/10/23 20:19
 */
@Component
@Order(50)
public class PhoneMaskDecorator implements FieldDecorator {
    private static final Pattern PHONE = Pattern.compile("(\\d{3})\\d{4}(\\d{4})");
    @Override
    public boolean applies(FieldMeta meta) {
        return "phone".equalsIgnoreCase(meta.name()) || "mobile".equalsIgnoreCase(meta.name());
    }

    @Override
    public JsonNode decorate(FieldMeta fieldMeta, JsonNode value, DecoratorContext context) {
        if (!value.isString()) {
            return null;
        }
        String masked = PHONE.matcher(value.asString()).replaceAll("$1****$2");
        return context.getNodeFactory().stringNode(masked);
    }
}

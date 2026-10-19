package com.zeeyeh.quyvera.common.advice;

import com.zeeyeh.quyvera.common.decorate.PayloadDecorator;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.JsonNode;

@RestControllerAdvice
@RequiredArgsConstructor
public class UnifiedResponseAdvice implements ResponseBodyAdvice<Object> {

    private final PayloadDecorator decorator;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return decorator.supports(converterType);
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        JsonNode decorated = decorator.decorate(body);
        return decorated != null ? decorated : body;
    }
}

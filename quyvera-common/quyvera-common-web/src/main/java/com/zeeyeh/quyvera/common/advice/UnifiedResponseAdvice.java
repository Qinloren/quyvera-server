package com.zeeyeh.quyvera.common.advice;

import com.zeeyeh.quyvera.common.annotations.SkipResultDecorate;
import com.zeeyeh.quyvera.common.annotations.SkipResultWrapper;
import com.zeeyeh.quyvera.common.core.entity.Result;
import com.zeeyeh.quyvera.common.decorate.PayloadDecorator;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.JsonNode;

import java.lang.annotation.Annotation;

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
        if (!(body instanceof Result<?>)
            && !Result.class.isAssignableFrom(returnType.getParameterType())
            && !hasAnnotation(SkipResultWrapper.class, returnType)) {
            body = Result.success(body);
        }
        if (hasAnnotation(SkipResultDecorate.class, returnType)) {
            return body;
        }
        JsonNode decorated = decorator.decorate(body);
        return decorated != null ? decorated : body;
    }

    /**
     * 校验方法获取所在类上是否存在指定注解
     * @param annotationType 注解类
     * @param returnType 返回类型
     * @return 是否存在
     */
    private boolean hasAnnotation(Class<? extends Annotation> annotationType, MethodParameter returnType) {
        return returnType.hasMethodAnnotation(annotationType) || AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), annotationType);
    }
}

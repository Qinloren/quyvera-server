package com.zeeyeh.common.plugin.aspect;

import com.zeeyeh.common.plugin.annotations.PluginEvent;
import com.zeeyeh.common.plugin.annotations.PluginParam;
import com.zeeyeh.common.plugin.exceptions.PluginEventVetoException;
import com.zeeyeh.common.plugin.publisher.PluginEventPublisher;
import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 插件事件触发切面
 * <p>
 * 参数处理规则：
 * <br>
 * 1. 简单类型：
 *    根据 Java 方法参数名称映射事件中的同名属性。
 * <br>
 * 2. 业务 Bean：
 *    使用 BeanUtils 在业务参数与事件之间双向复制属性。
 * <br>
 * 3. 基础设施参数：
 *    不参与事件属性复制，原样保留给业务方法。
 * <br>
 * 4. 无法识别的复杂参数：
 *    默认忽略，不将其当作业务 Bean。
 * @author Qinloren
 * @date 2026/11/16 14:33
 */
@Slf4j
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@Component
@RequiredArgsConstructor
public class PluginEventAspect {

    private static final String[] EXCLUDED_PROPERTIES = {
            "async",
            "vetoed"
    };

    private final PluginEventPublisher eventPublisher;

    @Pointcut(
            "@annotation(com.zeeyeh.common.plugin.annotations.PluginEvent) " +
                    "|| @annotation(com.zeeyeh.common.plugin.annotations.PluginEvents)"
    )
    public void pluginEventPointcut() {
    }

    @Around("pluginEventPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Method method = getTargetMethod(joinPoint);

        PluginEvent[] bindings = AnnotatedElementUtils
                .getMergedRepeatableAnnotations(method, PluginEvent.class)
                .toArray(new PluginEvent[0]);

        if (bindings.length == 0) {
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();

        // 1. 执行所有 PRE 事件。
        for (PluginEvent binding : bindings) {
            if (binding.phase() != PluginEvent.Phase.PRE) {
                continue;
            }

            Event event = createEvent(binding.value());

            // 将方法参数映射到事件。
            mapArgumentsToEvent(method, args, binding, event);

            // 同步发布，确保监听器修改能够立即生效。
            eventPublisher.publishSync(event);

            // PRE 事件允许取消业务操作。
            checkVeto(event);

            // 将事件修改后的数据回写到原方法参数。
            mapEventToArguments(method, args, binding, event);
        }

        // 2. 执行业务方法。
        Object result = joinPoint.proceed(args);

        // 3. 执行所有 POST 事件。
        // 只有业务方法正常执行后才会走到这里。
        for (PluginEvent binding : bindings) {
            if (binding.phase() != PluginEvent.Phase.POST) {
                continue;
            }

            Event event = createEvent(binding.value());

            // POST 参数映射：只复制，不回写。
            mapArgumentsToEvent(method, args, binding, event);

            // POST 返回值映射。
            mapReturnValueToEvent(
                    method,
                    result,
                    binding,
                    event
            );

            // POST 只通知，不允许取消已经执行的业务操作。
            eventPublisher.publishSync(event);
        }

        return result;
    }

    /**
     * 获取实际执行的方法。
     */
    private Method getTargetMethod(ProceedingJoinPoint joinPoint) {

        MethodSignature signature =
                (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        Object target = joinPoint.getTarget();

        if (target == null) {
            return method;
        }

        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);

        return AopUtils.getMostSpecificMethod(method, targetClass);
    }

    /**
     * 创建事件实例。
     */
    private Event createEvent(Class<? extends Event> eventClass) {

        try {
            return BeanUtils.instantiateClass(eventClass);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "无法创建插件事件实例：" + eventClass.getName(),
                    e
            );
        }
    }

    /**
     * 将方法参数映射到事件。
     * <p>
     * 规则：
     * 1. 无参数：不处理。
     * 2. 单参数且未配置 params：自动识别 PROPERTY / BEAN。
     * 3. 多参数且未配置 params：不处理任何参数。
     * 4. 配置 params：只处理明确指定的参数。
     */
    private void mapArgumentsToEvent(
            Method method,
            Object[] args,
            PluginEvent binding,
            Event event
    ) {

        Parameter[] parameters = method.getParameters();

        if (parameters.length == 0 || args.length == 0) {
            return;
        }

        Map<String, ParameterMapping> mappings =
                resolveParameterMappings(method, binding);

        for (ParameterMapping mapping : mappings.values()) {

            int index = mapping.index;

            if (index >= args.length) {
                continue;
            }

            Object argument = args[index];

            if (mapping.mode == PluginParam.Mode.PROPERTY) {

                copyPropertyToEvent(
                        argument,
                        parameters[index].getType(),
                        mapping.name,
                        event
                );

            } else {

                copyBeanProperties(argument, event);
            }
        }
    }

    /**
     * 将事件修改后的数据回写到方法参数。
     * <p>
     * PRE 专用。
     */
    private void mapEventToArguments(
            Method method,
            Object[] args,
            PluginEvent binding,
            Event event
    ) {

        Parameter[] parameters = method.getParameters();

        if (parameters.length == 0 || args.length == 0) {
            return;
        }

        Map<String, ParameterMapping> mappings =
                resolveParameterMappings(method, binding);

        for (ParameterMapping mapping : mappings.values()) {

            int index = mapping.index;

            if (index >= args.length) {
                continue;
            }

            if (mapping.mode == PluginParam.Mode.PROPERTY) {

                args[index] = copyPropertyFromEvent(
                        event,
                        mapping.name,
                        parameters[index].getType(),
                        args[index]
                );

            } else {

                copyBeanProperties(event, args[index]);
            }
        }
    }

    /**
     * 解析参数映射关系。
     */
    private Map<String, ParameterMapping> resolveParameterMappings(
            Method method,
            PluginEvent binding
    ) {

        Parameter[] parameters = method.getParameters();

        Map<String, ParameterMapping> result = new LinkedHashMap<>();

        PluginParam[] configuredParams = binding.params();

        // 单参数自动识别。
        if (parameters.length == 1 && configuredParams.length == 0) {

            Parameter parameter = parameters[0];

            String parameterName = getParameterName(parameter);

            PluginParam.Mode mode = isSimpleType(parameter.getType())
                    ? PluginParam.Mode.PROPERTY
                    : PluginParam.Mode.BEAN;

            result.put(
                    parameterName,
                    new ParameterMapping(0, parameterName, mode)
            );

            return result;
        }

        // 多参数没有配置时，不进行参数映射。
        if (configuredParams.length == 0) {
            return result;
        }

        // 根据显式配置解析参数。
        for (PluginParam configured : configuredParams) {

            String name = configured.name();

            if (!StringUtils.isBlank(name)) {
                throw new IllegalArgumentException(
                        "@PluginEvent 的参数名称不能为空：" + method
                );
            }

            int index = findParameterIndex(parameters, name);

            if (index < 0) {
                throw new IllegalArgumentException(
                        "方法 " + method.getName()
                                + " 中找不到参数：" + name
                                + "，请检查参数名称及 -parameters 编译配置"
                );
            }

            if (result.containsKey(name)) {
                throw new IllegalArgumentException(
                        "@PluginEvent 重复配置参数：" + name
                );
            }

            result.put(
                    name,
                    new ParameterMapping(
                            index,
                            name,
                            configured.mode()
                    )
            );
        }

        return result;
    }

    /**
     * 获取方法参数名称。
     */
    private String getParameterName(Parameter parameter) {

        if (!parameter.isNamePresent()) {
            throw new IllegalStateException(
                    "无法获取方法参数名称，请在 Maven Compiler Plugin 中配置 "
                            + "<parameters>true</parameters>"
            );
        }

        return parameter.getName();
    }

    /**
     * 查找方法参数下标。
     */
    private int findParameterIndex(
            Parameter[] parameters,
            String name
    ) {

        for (int i = 0; i < parameters.length; i++) {

            if (name.equals(getParameterName(parameters[i]))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * PROPERTY 模式：
     * 将参数值写入事件的同名属性。
     */
    private void copyPropertyToEvent(
            Object value,
            Class<?> sourceType,
            String propertyName,
            Object event
    ) {

        if (EXCLUDED_PROPERTIES[0].equals(propertyName)) {
            return;
        }

        BeanWrapper eventWrapper = new BeanWrapperImpl(event);

        if (!eventWrapper.isWritableProperty(propertyName)) {
            return;
        }

        Class<?> targetType =
                eventWrapper.getPropertyType(propertyName);

        if (targetType == null
                || isAssignable(targetType, sourceType)) {
            return;
        }

        if (value == null && targetType.isPrimitive()) {
            return;
        }

        eventWrapper.setPropertyValue(propertyName, value);
    }

    /**
     * PROPERTY 模式：
     * 将事件属性回写到方法参数。
     * <p>
     * 简单类型参数通过替换 args[index] 实现回写。
     */
    private Object copyPropertyFromEvent(
            Object event,
            String propertyName,
            Class<?> targetType,
            Object originalValue
    ) {

        if (EXCLUDED_PROPERTIES[0].equals(propertyName)) {
            return originalValue;
        }

        BeanWrapper eventWrapper = new BeanWrapperImpl(event);

        if (!eventWrapper.isReadableProperty(propertyName)) {
            return originalValue;
        }

        Class<?> sourceType =
                eventWrapper.getPropertyType(propertyName);

        if (sourceType == null
                || isAssignable(targetType, sourceType)) {
            return originalValue;
        }

        Object value = eventWrapper.getPropertyValue(propertyName);

        if (value == null && targetType.isPrimitive()) {
            return originalValue;
        }

        return value;
    }

    /**
     * BEAN 模式：
     * 将 source 中与 target 同名且类型兼容的属性复制到 target。
     * <p>
     * 不使用 BeanUtils.copyProperties 的默认宽松处理，
     * 这里明确检查属性类型，并排除 async。
     */
    private void copyBeanProperties(
            Object source,
            Object target
    ) {

        if (source == null || target == null) {
            return;
        }

        BeanWrapper sourceWrapper = new BeanWrapperImpl(source);
        BeanWrapper targetWrapper = new BeanWrapperImpl(target);

        for (var sourceDescriptor :
                sourceWrapper.getPropertyDescriptors()) {

            String propertyName = sourceDescriptor.getName();

            if (isExcludedProperty(propertyName)) {
                continue;
            }

            if (!sourceWrapper.isReadableProperty(propertyName)
                    || !targetWrapper.isWritableProperty(propertyName)) {
                continue;
            }

            Class<?> sourceType =
                    sourceWrapper.getPropertyType(propertyName);

            Class<?> targetType =
                    targetWrapper.getPropertyType(propertyName);

            if (sourceType == null || targetType == null) {
                continue;
            }

            if (isAssignable(targetType, sourceType)) {
                continue;
            }

            Object value =
                    sourceWrapper.getPropertyValue(propertyName);

            if (value == null && targetType.isPrimitive()) {
                continue;
            }

            targetWrapper.setPropertyValue(propertyName, value);
        }
    }

    /**
     * 将业务方法返回值映射到 POST 事件。
     * <p>
     * DTO / Bean：
     * 自动按照同名属性拆分映射。
     * <p>
     * 简单类型：
     * 使用 returnProperty 指定目标属性。
     */
    private void mapReturnValueToEvent(
            Method method,
            Object result,
            PluginEvent binding,
            Event event
    ) {

        if (method.getReturnType() == void.class
                || method.getReturnType() == Void.class) {
            return;
        }

        if (result == null) {
            return;
        }

        Class<?> returnType = result.getClass();

        if (isSimpleType(returnType)) {

            String propertyName = binding.returnProperty();

            // 简单类型没有可拆分的 Bean 属性。
            // 没有指定目标属性时，不映射。
            if (!StringUtils.isBlank(propertyName)) {
                return;
            }

            copyPropertyToEvent(
                    result,
                    returnType,
                    propertyName,
                    event
            );

            return;
        }

        // DTO / 普通 Bean：拆分属性映射，不嵌套整个返回对象。
        copyBeanProperties(result, event);
    }

    /**
     * 检查 PRE 事件是否被取消。
     */
    private void checkVeto(Event event) {

        if (!(event instanceof Vetoable vetoable)) {
            return;
        }

        if (!vetoable.isVetoed()) {
            return;
        }

        String reason = vetoable.getVetoReason();

        if (!StringUtils.isBlank(reason)) {
            reason = "插件事件已取消，业务操作终止";
        }

        throw new PluginEventVetoException(reason);
    }

    /**
     * 判断类型是否兼容。
     * <p>
     * 同时兼容 primitive 和包装类型。
     */
    private boolean isAssignable(
            Class<?> targetType,
            Class<?> sourceType
    ) {

        return !ClassUtils.isAssignable(
                ClassUtils.resolvePrimitiveIfNecessary(targetType),
                ClassUtils.resolvePrimitiveIfNecessary(sourceType)
        );
    }

    /**
     * 判断是否为简单类型。
     */
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || Number.class.isAssignableFrom(type)
                || CharSequence.class.isAssignableFrom(type)
                || Boolean.class == type
                || Character.class == type
                || Date.class.isAssignableFrom(type)
                || Temporal.class.isAssignableFrom(type)
                || UUID.class == type
                || URI.class == type
                || URL.class == type
                || BigDecimal.class == type
                || BigInteger.class == type;
    }

    /**
     * 判断是否为排除属性。
     */
    private boolean isExcludedProperty(String propertyName) {

        for (String excluded : EXCLUDED_PROPERTIES) {
            if (excluded.equals(propertyName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 参数映射描述。
     */
    private static class ParameterMapping {

        private final int index;

        private final String name;

        private final PluginParam.Mode mode;

        private ParameterMapping(
                int index,
                String name,
                PluginParam.Mode mode
        ) {
            this.index = index;
            this.name = name;
            this.mode = mode;
        }
    }
}

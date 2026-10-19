package com.zeeyeh.quyvera.common.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.zeeyeh.quyvera.common.core.entity.Result;
import com.zeeyeh.quyvera.common.core.exception.BusinessExceptions;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 统一异常处理类
 * @author Qinloren
 */
@Slf4j
@RestControllerAdvice
public class UnifiedExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址: \"{}\",不支持的请求方法: \"{}\"", requestURI, e.getMethod());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(BusinessExceptions.class)
    public Result<Void> handleBusinessException(BusinessExceptions e, HttpServletRequest request) {
        log.error(e.getDetailMessage());
        Integer code = e.getCode();
        return ObjectUtil.isNotNull(code) ? Result.any(code, e.getMessage(), null) : Result.error(e.getMessage());
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public Result<Void> handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址: \"{}\", 缺少的路径变量: \"{}\"", requestURI, e.getVariableName());
        return Result.error("请求地址不合法");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址: \"{}\", 参数类型不匹配", requestURI);
        return Result.error("参数不合法");
    }

    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public Result<Void> handleNoHandlerFoundException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址: \"{}\", 不存在", requestURI);
        return Result.error("请求地址不合法");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String errorCode = RandomUtil.randomNumbers(12);
        log.error("请求地址: \"{}\", 发送未知异常, 错误编号: \"{}\"", requestURI, errorCode);
        log.error("堆栈跟踪: ", e);
        return Result.error("发生未知异常, 错误码: " + errorCode);
    }
}

package com.zeeyeh.quyvera.common.entity;

import com.zeeyeh.quyvera.common.constant.RequestCodeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应内容结构体
 * @param <T> 数据类型
 * @author Qinloren
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int code;

    private String message;

    private T data;

    private long timestamp;

    /**
     * 构建成功响应结果
     * @return 成功响应结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> success() {
        return any(RequestCodeStatus.SUCCESS, "successfully", null);
    }

    /**
     * 构建成功响应结果
     * @param message 响应消息
     * @return 成功响应结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> success(String message) {
        return any(RequestCodeStatus.SUCCESS, message, null);
    }

    /**
     * 构建成功响应结果
     * @param data 响应数据
     * @return 成功响应结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> success(T data) {
        return any(RequestCodeStatus.SUCCESS, "successfully", data);
    }

    /**
     * 构建成功响应结果
     * @param message 响应消息
     * @param data 响应数据
     * @return 成功响应结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> success(String message, T data) {
        return any(RequestCodeStatus.SUCCESS, message, data);
    }

    /**
     * 构建失败响应结果
     * @return 失败响应结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> error() {
        return any(RequestCodeStatus.ERROR, "unsuccessfully", null);
    }

    /**
     * 构建失败响应结果
     * @param message 响应消息
     * @return 失败响应结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> error(String message) {
        return any(RequestCodeStatus.ERROR, message, null);
    }

    /**
     * 构建失败响应结果
     * @param data 响应数据
     * @return 失败响应结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> error(T data) {
        return any(RequestCodeStatus.ERROR, "unsuccessfully", data);
    }

    /**
     * 构建失败响应结果
     * @param message 响应消息
     * @param data 响应数据
     * @return 失败响应结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> error(String message, T data) {
        return any(RequestCodeStatus.ERROR, message, data);
    }

    /**
     * 构建自定义响应结果
     * @param code 状态码
     * @param message 响应消息
     * @param data 响应数据
     * @return 响应结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> any(int code, String message, T data) {
        return new Result<T>(code, message, data, System.currentTimeMillis());
    }

    /**
     * 校验响应结果是否成功
     * @param result 响应结果
     * @return 是否成功
     * @param <T> 数据类型
     */
    public static <T> Boolean isSuccess(Result<T> result) {
        if (result == null) {
            return false;
        }
        return RequestCodeStatus.SUCCESS == result.getCode();
    }

    /**
     * 校验响应结果是否成功
     * @param result 响应结果
     * @return 是否成功
     * @param <T> 数据类型
     */
    public static <T> Boolean isError(Result<T> result) {
        if (result == null) {
            return false;
        }
        return RequestCodeStatus.ERROR == result.getCode();
    }
}

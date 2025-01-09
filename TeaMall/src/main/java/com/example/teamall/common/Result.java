/**
 * 通用返回结果类，封装了API响应的标准格式
 * @param <T> 返回数据的类型
 */
package com.example.teamall.common;

import lombok.Data;

@Data
public class Result<T> {
    /** 响应状态码 */
    private Integer code;
    /** 响应信息 */
    private String message;
    /** 响应数据 */
    private T data;

    /**
     * 私有构造函数，通过静态方法创建实例
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     */
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 创建成功响应（无数据）
     * @return 成功响应结果
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 创建成功响应（包含数据）
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 创建成功响应（自定义消息和数据）
     * @param message 自定义消息
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 创建错误响应（仅包含错误消息）
     * @param message 错误消息
     * @return 错误响应结果
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 创建错误响应（自定义状态码和错误消息）
     * @param code 错误状态码
     * @param message 错误消息
     * @return 错误响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
} 
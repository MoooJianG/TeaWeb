/**
 * 业务异常类
 * 用于封装业务逻辑异常，包含错误码和错误信息
 * 继承自RuntimeException，使其成为非受检异常
 */
package com.example.teamall.exception;

public class BusinessException extends RuntimeException {
    /** 错误码 */
    private Integer code;

    /**
     * 创建业务异常
     * 使用默认错误码500
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 创建业务异常
     * 使用自定义错误码
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取错误码
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }
} 
/**
 * 全局异常处理器
 * 统一处理系统中抛出的各类异常，将其转换为标准的响应格式
 * 使用@RestControllerAdvice注解，对所有Controller层抛出的异常进行处理
 */
package com.example.teamall.exception;

import com.example.teamall.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 将BusinessException转换为标准响应格式
     * @param e 业务异常
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理其他未知异常
     * 统一返回500错误，避免系统异常细节泄露
     * @param e 未知异常
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        // 打印异常堆栈，方便排查问题
        e.printStackTrace();
        return Result.error("系统错误，请稍后重试");
    }
} 
/**
 * 响应状态码枚举类
 * 定义了系统中所有的响应状态码及其对应的消息
 */
package com.example.teamall.common;

public enum ResultCode {
    /** 操作成功 */
    SUCCESS(200, "操作成功"),
    /** 操作失败 */
    ERROR(500, "操作失败"),
    /** 参数验证失败 */
    VALIDATE_FAILED(404, "参数检验失败"),
    /** 未登录或token过期 */
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    /** 权限不足 */
    FORBIDDEN(403, "没有相关权限"),
    /** 访问被拒绝 */
    ACCESS_DENIED(403, "无权限访问"),
    /** 管理员专用功能 */
    ADMIN_ONLY(403, "仅管理员可访问");

    /** 状态码 */
    private Integer code;
    /** 状态描述 */
    private String message;

    /**
     * 构造函数
     * @param code 状态码
     * @param message 状态描述
     */
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取状态码
     * @return 状态码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取状态描述
     * @return 状态描述
     */
    public String getMessage() {
        return message;
    }
} 
/**
 * 登录请求数据传输对象
 * 用于接收前端登录请求时提交的用户凭证
 */
package com.example.teamall.dto;

import lombok.Data;

@Data
public class LoginRequest {
    /** 用户邮箱 */
    private String email;
    
    /** 用户密码 */
    private String password;

    public String getEmail() {
        return this.email;
    }
} 
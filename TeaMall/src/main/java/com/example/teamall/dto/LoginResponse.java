/**
 * 登录响应数据传输对象
 * 用于向前端返回登录成功后的用户信息和认证token
 */
package com.example.teamall.dto;

import com.example.teamall.model.User;
import lombok.Data;

@Data
public class LoginResponse {
    /** 用户信息 */
    private User user;
    
    /** JWT认证token */
    private String token;
} 
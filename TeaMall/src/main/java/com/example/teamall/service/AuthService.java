/**
 * 认证服务接口
 * 提供用户认证相关的业务逻辑定义
 * 包括登录、注册、登出等功能
 */
package com.example.teamall.service;

import com.example.teamall.dto.LoginRequest;
import com.example.teamall.dto.RegisterRequest;
import com.example.teamall.common.Result;

public interface AuthService {
    /**
     * 用户登录
     * @param request 登录请求，包含用户名和密码
     * @return 登录结果，包含用户信息和JWT令牌
     */
    Result<?> login(LoginRequest request);

    /**
     * 用户注册
     * @param request 注册请求，包含用户基本信息
     * @return 注册结果，包含注册成功提示
     */
    Result<?> register(RegisterRequest request);

    /**
     * 用户登出
     * @return 登出结果，包含登出成功提示
     */
    Result<?> logout();
} 
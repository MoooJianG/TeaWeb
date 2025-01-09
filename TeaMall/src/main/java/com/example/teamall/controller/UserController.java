/**
 * 用户管理控制器
 * 处理用户相关的HTTP请求，包括：
 * - 用户注册
 * - 用户登录
 * - 用户信息管理
 * - 密码修改
 * - 用户注销
 */
package com.example.teamall.controller;

import com.example.teamall.common.Result;
import com.example.teamall.dto.LoginRequest;
import com.example.teamall.dto.LoginResponse;
import com.example.teamall.model.User;
import com.example.teamall.service.UserService;
import com.example.teamall.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    /** 用户服务 */
    @Autowired
    private UserService userService;
    
    /** JWT工具类 */
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * 获取所有用户列表
     * 仅管理员可访问
     * @return 用户列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.findAll());
    }
    
    /**
     * 获取当前登录用户信息
     * @param userDetails 当前登录用户详情
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(user);
    }
    
    /**
     * 创建新用户（管理员功能）
     * @param user 用户信息
     * @return 创建的用户信息
     */
    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        if (userService.existsByUsername(user.getUsername())) {
            return Result.error("用户名已存在");
        }
        return Result.success("用户创建成功", userService.save(user));
    }
    
    /**
     * 用户注册
     * @param user 注册信息
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        if (userService.existsByUsername(user.getUsername())) {
            return Result.error("用户名已存在");
        }
        if (userService.existsByEmail(user.getEmail())) {
            return Result.error("邮箱已被使用");
        }
        return Result.success("注册成功", userService.save(user));
    }
    
    /**
     * 用户登录
     * @param loginRequest 登录请求（包含邮箱和密码）
     * @return 登录响应（包含用户信息和JWT token）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
        String token = jwtUtils.generateToken(user);
        
        LoginResponse response = new LoginResponse();
        response.setUser(user);
        response.setToken(token);
        
        return Result.success("登录成功", response);
    }
    
    /**
     * 更新用户信息
     * 可更新的字段包括：用户名、电话、头像URL、真实姓名
     * @param userDetails 当前登录用户
     * @param updateUser 要更新的用户信息
     * @return 更新后的用户信息
     */
    @PutMapping("/info")
    public Result<User> updateUserInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody User updateUser) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        // 检查并更新用户名
        if (updateUser.getUsername() != null) {
            log.debug("Old username: {}", currentUser.getUsername());
            log.debug("New username: {}", updateUser.getUsername());
            // 如果新用户名与当前用户名不同，需要检查唯一性
            if (!updateUser.getUsername().equals(currentUser.getUsername())) {
                if (userService.existsByUsername(updateUser.getUsername())) {
                    return Result.error("用户名已存在");
                }
                currentUser.setUsername(updateUser.getUsername());
            }
            log.debug("Updated username: {}", currentUser.getUsername());
        }
        
        // 只允许更新部分字段
        if (updateUser.getPhone() != null) {
            currentUser.setPhone(updateUser.getPhone());
        }
        if (updateUser.getAvatarUrl() != null) {
            currentUser.setAvatarUrl(updateUser.getAvatarUrl());
        }
        if (updateUser.getRealName() != null) {
            currentUser.setRealName(updateUser.getRealName());
        }
        
        return Result.success(userService.save(currentUser));
    }
    
    /**
     * 修改密码
     * @param userDetails 当前登录用户
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PutMapping("/password")
    public Result<String> updatePassword(@AuthenticationPrincipal UserDetails userDetails, 
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        return Result.success(userService.updatePassword(userDetails.getUsername(), oldPassword, newPassword));
    }
    
    /**
     * 用户注销
     * 由于使用JWT，服务端不需要额外处理，客户端删除token即可
     * @param userDetails 当前登录用户
     * @return 注销结果
     */
    @PostMapping("/logout")
    public Result<String> logout(@AuthenticationPrincipal UserDetails userDetails) {
        // JWT是无状态的，客户端只需要删除token即可
        return Result.success("注销成功");
    }
} 
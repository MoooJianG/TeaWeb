/**
 * JSON格式登录认证过滤器
 * 用于处理JSON格式的登录请求，继承UsernamePasswordAuthenticationFilter
 * 将JSON格式的登录请求转换为Spring Security可以处理的认证token
 */
package com.example.teamall.config;

import com.example.teamall.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class JsonAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    
    /**
     * 尝试认证
     * 将JSON格式的登录请求转换为UsernamePasswordAuthenticationToken
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 认证对象
     * @throws AuthenticationException 认证异常
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            // 从请求体中读取并解析登录信息
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
            
            // 创建认证token
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword());
            
            // 设置认证详情
            setDetails(request, authRequest);
            
            // 执行认证
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
} 
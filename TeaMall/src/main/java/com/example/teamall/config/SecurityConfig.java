/**
 * Spring Security 配置类
 * 负责配置系统的安全认证、授权、会话管理等核心安全功能
 */
package com.example.teamall.config;

import com.example.teamall.common.Result;
import com.example.teamall.model.User;
import com.example.teamall.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.teamall.util.JwtUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity  // 启用Spring Security的Web安全支持
public class SecurityConfig {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /** Spring Security认证配置 */
    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    /** 用户服务 */
    @Autowired
    private UserService userService;

    /** JSON处理工具 */
    @Autowired
    private ObjectMapper objectMapper;

    /** JWT工具类 */
    @Autowired
    private JwtUtils jwtUtils;

    /** JWT认证过滤器 */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置认证管理器
     * @param config 认证配置
     * @return AuthenticationManager实例
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置安全过滤链
     * 定义系统的安全规则，包括：
     * - CSRF防护
     * - CORS配置
     * - 会话管理
     * - 请求授权规则
     * - JWT过滤器
     * - 异常处理
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF防护
            .csrf(csrf -> csrf.disable())
            // 启用CORS支持
            .cors(cors -> cors.configure(http))
            // 配置会话管理，使用无状态会话
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth
                // 允许未认证访问的公开接口
                .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                // 管理员专用接口
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 需要认证的用户接口
                .requestMatchers("/api/users/info").authenticated()
                .requestMatchers("/api/orders/**").authenticated()
                .requestMatchers("/api/cart/**").authenticated()
                .requestMatchers("/api/addresses/**").authenticated()
                // 公开接口
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 配置异常处理
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\"}");
                })
            );
            
        return http.build();
    }
} 
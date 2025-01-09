/**
 * 密码编码器配置类
 * 配置系统使用的密码加密方式，使用BCrypt加密算法
 */
package com.example.teamall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    
    /**
     * 配置密码编码器
     * 使用BCrypt加密算法，这是一种单向Hash加密算法，
     * 专门针对密码存储设计，包含加盐和多轮Hash等安全特性
     * 
     * @return PasswordEncoder BCrypt密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 
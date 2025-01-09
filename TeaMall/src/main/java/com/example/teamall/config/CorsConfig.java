/**
 * 跨域资源共享(CORS)配置类
 * 配置允许跨域请求的相关参数，使前端能够正常访问后端API
 */
package com.example.teamall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    /**
     * 配置CORS过滤器
     * 设置跨域请求的允许参数，包括：
     * - 允许的源
     * - 允许的请求头
     * - 允许的请求方法
     * - 是否允许携带认证信息
     * 
     * @return CorsFilter CORS过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有源的跨域请求
        config.addAllowedOriginPattern("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有HTTP方法
        config.addAllowedMethod("*");
        // 允许请求携带认证信息（cookies等）
        config.setAllowCredentials(true);
        
        // 对所有路径应用这些CORS配置
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 
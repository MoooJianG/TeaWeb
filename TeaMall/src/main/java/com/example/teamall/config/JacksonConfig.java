/**
 * Jackson JSON配置类
 * 配置JSON序列化和反序列化的行为，特别是日期时间的处理方式
 */
package com.example.teamall.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {
    
    /** 日期时间格式化模式 */
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 配置ObjectMapper
     * 自定义JSON序列化和反序列化的行为：
     * 1. 配置LocalDateTime的序列化格式
     * 2. 禁用将日期序列化为时间戳的功能
     * 
     * @return ObjectMapper 配置好的ObjectMapper实例
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 配置LocalDateTime序列化格式为指定的日期时间模式
        javaTimeModule.addSerializer(LocalDateTime.class, 
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        
        // 注册JavaTimeModule
        objectMapper.registerModule(javaTimeModule);
        // 禁用将日期序列化为时间戳的功能
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return objectMapper;
    }
} 
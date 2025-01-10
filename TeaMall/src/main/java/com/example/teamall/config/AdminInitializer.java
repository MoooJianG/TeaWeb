package com.example.teamall.config;

import com.example.teamall.model.User;
import com.example.teamall.model.UserRole;
import com.example.teamall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) {
        // 检查是否已存在管理员账号
        if (!userService.existsByEmail("admin@teamall.com")) {
            // 创建管理员用户对象
            User admin = new User();
            admin.setUsername("admin123");
            admin.setEmail("admin@teamall.com");
            admin.setPassword("admin123");
            admin.setPhone("13800000000");
            admin.setRealName("系统管理员");
            admin.setRole(UserRole.ROLE_ADMIN);
            
            // 使用 save 方法保存管理员账号
            userService.save(admin);
            
            System.out.println("系统初始化: 管理员账号已创建");
            System.out.println("账号: admin@teamall.com");
            System.out.println("密码: admin123");
        }
    }
} 
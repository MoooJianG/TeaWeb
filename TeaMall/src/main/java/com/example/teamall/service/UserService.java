/**
 * 用户服务类
 * 提供用户相关的业务逻辑处理
 * 包括用户注册、登录、密码管理等功能
 */
package com.example.teamall.service;

import com.example.teamall.model.User;
import com.example.teamall.model.UserRole;
import com.example.teamall.repository.UserRepository;
import com.example.teamall.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    
    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    /** 用户数据访问接口 */
    @Autowired
    private UserRepository userRepository;
    
    /** 密码加密器 */
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象，如果不存在则返回null
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElse(null);
    }
    
    /**
     * 保存用户信息
     * 如果是新用户，会进行密码加密、初始化积分和会员等级等操作
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    public User save(User user) {
        boolean isNewUser = !userRepository.existsByEmail(user.getEmail());
        
        if (isNewUser) {
            log.info("New user - encrypting password");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setPoints(0);
            user.setMemberLevel(0);
            user.setCreatedAt(LocalDateTime.now());
            // 如果未设置角色，默认为普通用户
            if (user.getRole() == null) {
                user.setRole(UserRole.ROLE_USER);
            }
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * 查找所有用户
     * @return 用户列表
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return true表示已存在，false表示不存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否已被注册
     * @param email 邮箱
     * @return true表示已注册，false表示未注册
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 创建管理员账户
     * @param user 用户对象
     * @return 创建后的管理员用户对象
     */
    public User createAdmin(User user) {
        user.setRole(UserRole.ROLE_ADMIN);
        return save(user);
    }

    /**
     * 用户登录
     * 验证邮箱和密码是否匹配
     * @param email 邮箱
     * @param password 密码
     * @return 登录成功的用户对象
     * @throws BusinessException 当邮箱或密码错误时抛出异常
     */
    public User login(String email, String password) {
        log.info("Login attempt - email: {}, password: {}", email, password);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("邮箱或密码错误"));
        
        log.info("Found user: {}, stored password: {}", user.getEmail(), user.getPassword());
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("邮箱或密码错误");
        }
        
        return user;
    }

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户对象，如果不存在则返回null
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElse(null);
    }

    /**
     * 更新用户密码
     * 验证原密码是否正确，然后更新为新密码
     * @param username 用户名
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 更新成功的提示信息
     * @throws BusinessException 当用户不存在或原密码错误时抛出异常
     */
    public String updatePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException("用户不存在"));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "密码修改成功";
    }

    /**
     * 检查用户是否为管理员
     * @param user 用户对象
     * @return true表示是管理员，false表示不是管理员
     */
    public boolean isAdmin(User user) {
        return user.getRole() == UserRole.ROLE_ADMIN;
    }
} 
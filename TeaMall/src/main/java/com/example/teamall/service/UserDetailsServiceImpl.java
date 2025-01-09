/**
 * Spring Security用户详情服务实现类
 * 实现UserDetailsService接口，提供用户认证信息的加载
 * 用于Spring Security的用户认证过程
 */
package com.example.teamall.service;

import com.example.teamall.model.User;
import com.example.teamall.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    /** 用户数据访问接口 */
    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户名加载用户认证信息
     * 实现UserDetailsService接口的方法
     * 将系统用户转换为Spring Security的UserDetails对象
     * @param username 用户名
     * @return UserDetails对象，包含用户认证和授权信息
     * @throws UsernameNotFoundException 当用户不存在时抛出异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        log.info("Found user: {}, role: {}", user.getUsername(), user.getRole());
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
} 
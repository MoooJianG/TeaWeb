/**
 * 用户实体类
 * 实现了Spring Security的UserDetails接口，用于认证和授权
 * 包含用户的基本信息、会员信息和账户状态
 */
package com.example.teamall.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {
    /** 用户ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 用户名，唯一 */
    @Column(unique = true)
    private String username;
    
    /** 邮箱，唯一 */
    @Column(unique = true)
    private String email;
    
    /** 密码（加密存储） */
    private String password;
    
    /** 手机号码 */
    private String phone;
    
    /** 真实姓名 */
    @Column(name = "real_name")
    private String realName;
    
    /** 头像URL */
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    /** 积分 */
    private Integer points;
    
    /** 会员等级 */
    @Column(name = "member_level")
    private Integer memberLevel;
    
    /** 用户角色（USER/ADMIN） */
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    /** 创建时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 获取用户名
     * 实现UserDetails接口方法
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * 获取用户权限
     * 实现UserDetails接口方法
     * 将用户角色转换为Spring Security的权限对象
     * @return 权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * 账户是否未过期
     * 实现UserDetails接口方法
     * @return true表示未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户是否未锁定
     * 实现UserDetails接口方法
     * @return true表示未锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证是否未过期
     * 实现UserDetails接口方法
     * @return true表示未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否启用
     * 实现UserDetails接口方法
     * @return true表示启用
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 设置用户名
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取邮箱
     * @return 邮箱地址
     */
    public String getEmail() {
        return this.email;
    }
} 
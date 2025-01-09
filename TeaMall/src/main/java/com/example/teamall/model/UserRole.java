/**
 * 用户角色枚举类
 * 定义系统中的用户角色类型
 * 用于用户权限管理和访问控制
 */
package com.example.teamall.model;

public enum UserRole {
    /** 普通用户角色，具有基本的操作权限 */
    ROLE_USER,
    
    /** 管理员角色，具有系统管理权限 */
    ROLE_ADMIN
} 
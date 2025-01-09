/**
 * 用户数据访问接口
 * 提供对User实体的基本CRUD操作和自定义查询方法
 * 继承JpaRepository以获取基本的数据库操作功能
 */
package com.example.teamall.repository;

import com.example.teamall.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 根据邮箱查找用户
     * @param email 用户邮箱
     * @return 包含用户信息的Optional对象
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 包含用户信息的Optional对象
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查邮箱是否已被注册
     * @param email 待检查的邮箱
     * @return true表示邮箱已存在，false表示邮箱可用
     */
    boolean existsByEmail(String email);

    /**
     * 检查用户名是否已被使用
     * @param username 待检查的用户名
     * @return true表示用户名已存在，false表示用户名可用
     */
    boolean existsByUsername(String username);
} 
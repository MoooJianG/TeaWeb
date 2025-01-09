/**
 * 购物车项数据访问接口
 * 提供对CartItem实体的基本CRUD操作和自定义查询方法
 * 继承JpaRepository以获取基本的数据库操作功能
 */
package com.example.teamall.repository;

import com.example.teamall.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    /**
     * 查询用户的购物车列表，按创建时间倒序排序
     * @param userId 用户ID
     * @return 购物车项列表
     */
    List<CartItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 查询用户购物车中的特定商品
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 包含购物车项信息的Optional对象
     */
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * 从用户购物车中删除特定商品
     * @param userId 用户ID
     * @param productId 商品ID
     */
    void deleteByUserIdAndProductId(Long userId, Long productId);
} 
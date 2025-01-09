/**
 * 商品评价数据访问接口
 * 提供对Review实体的基本CRUD操作和自定义查询方法
 * 继承JpaRepository以获取基本的数据库操作功能
 */
package com.example.teamall.repository;

import com.example.teamall.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * 分页查询商品的评价列表
     * @param productId 商品ID
     * @param pageable 分页参数
     * @return 评价分页结果
     */
    Page<Review> findByProductId(Long productId, Pageable pageable);

    /**
     * 分页查询用户的评价列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 评价分页结果
     */
    Page<Review> findByUserId(Long userId, Pageable pageable);

    /**
     * 检查用户是否已对订单进行评价
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return true表示已评价，false表示未评价
     */
    boolean existsByUserIdAndOrderId(Long userId, Long orderId);
} 
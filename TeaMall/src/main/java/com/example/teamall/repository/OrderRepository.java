/**
 * 订单数据访问接口
 * 提供对Order实体的基本CRUD操作和自定义查询方法
 * 继承JpaRepository以获取基本的数据库操作功能
 */
package com.example.teamall.repository;

import com.example.teamall.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.teamall.model.OrderStatus;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * 根据用户ID分页查询订单列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 订单分页结果
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据订单编号查询订单
     * @param orderNo 订单编号
     * @return 订单信息
     */
    Order findByOrderNo(String orderNo);
    
    /**
     * 根据订单ID查询订单详情，包括订单项和商品信息
     * 使用LEFT JOIN FETCH优化N+1查询问题
     * @param id 订单ID
     * @return 包含完整订单信息的Optional对象
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
    
    /**
     * 根据多个条件分页查询用户订单
     * 支持按订单编号、订单状态、创建时间范围进行筛选
     * @param userId 用户ID
     * @param orderNo 订单编号（可选）
     * @param status 订单状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageable 分页参数
     * @return 订单分页结果
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId " +
           "AND (:orderNo IS NULL OR o.orderNo = :orderNo) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:startTime IS NULL OR o.createTime >= :startTime) " +
           "AND (:endTime IS NULL OR o.createTime <= :endTime)")
    Page<Order> findByUserAndConditions(
           @Param("userId") Long userId, 
           @Param("orderNo") String orderNo, 
           @Param("status") OrderStatus status, 
           @Param("startTime") LocalDateTime startTime, 
           @Param("endTime") LocalDateTime endTime, 
           Pageable pageable);
} 
/**
 * 商品数据访问接口
 * 提供对Product实体的基本CRUD操作和自定义查询方法
 * 继承JpaRepository以获取基本的数据库操作功能
 */
package com.example.teamall.repository;

import com.example.teamall.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    /**
     * 使用悲观锁查询商品信息
     * 用于并发场景下的库存管理
     * @param id 商品ID
     * @return 包含商品信息的Optional对象
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    /**
     * 多条件分页搜索商品
     * 支持按分类、关键词、价格范围进行筛选
     * 使用原生SQL查询以支持全文搜索
     * @param categoryId 分类ID（可选）
     * @param keyword 搜索关键词（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    @Query(value = "SELECT p.* FROM products p WHERE " +
           "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
           "(:keyword IS NULL OR CAST(p.name AS text) ILIKE CONCAT('%', CAST(:keyword AS text), '%') OR CAST(p.description AS text) ILIKE CONCAT('%', CAST(:keyword AS text), '%')) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "ORDER BY p.id DESC",
           countQuery = "SELECT COUNT(*) FROM products p WHERE " +
           "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
           "(:keyword IS NULL OR CAST(p.name AS text) ILIKE CONCAT('%', CAST(:keyword AS text), '%') OR CAST(p.description AS text) ILIKE CONCAT('%', CAST(:keyword AS text), '%')) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)",
           nativeQuery = true)
    Page<Product> search(@Param("categoryId") Long categoryId,
                        @Param("keyword") String keyword,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        Pageable pageable);
} 
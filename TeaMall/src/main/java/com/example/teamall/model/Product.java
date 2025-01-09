/**
 * 商品实体类
 * 包含商品的基本信息、价格、库存、分类等
 * 与Category实体类存在多对一关系
 */
package com.example.teamall.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "products")
public class Product {
    /** 商品ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 商品名称 */
    private String name;
    
    /** 商品描述 */
    private String description;
    
    /** 商品价格 */
    private BigDecimal price;
    
    /** 商品库存 */
    private Integer stock;
    
    /** 商品分类 */
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    /** 商品状态（上架、下架等） */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductStatus status = ProductStatus.ON_SALE;
    
    /** 商品图片URL */
    private String imageUrl;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
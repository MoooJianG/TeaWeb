/**
 * 购物车项实体类
 * 用于记录用户购物车中的商品信息
 * 与User、Product实体类存在多对一关系
 */
package com.example.teamall.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cart_items")
public class CartItem {
    /** 购物车项ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 所属用户 */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    /** 商品信息 */
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    /** 商品数量 */
    private Integer quantity;
    
    /** 是否选中，用于结算时的选择 */
    private Boolean selected = true;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
    
    /**
     * 创建时自动设置创建时间和更新时间
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新时自动设置更新时间
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 
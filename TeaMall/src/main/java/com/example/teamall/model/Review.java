/**
 * 商品评价实体类
 * 用于记录用户对商品的评价信息
 * 与User、Product、Order实体类存在多对一关系
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
@Table(name = "reviews")
public class Review {
    /** 评价ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 评价用户 */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    /** 评价商品 */
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    /** 关联订单 */
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    /** 评分（1-5星） */
    @Column(nullable = false)
    private Integer rating;
    
    /** 评价内容 */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /** 评价图片数组 */
    @Column(columnDefinition = "TEXT[]")
    private String[] images;
    
    /** 商家回复内容 */
    @Column(columnDefinition = "TEXT")
    private String reply;
    
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
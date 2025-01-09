/**
 * 订单项实体类
 * 表示订单中的单个商品项
 * 与Order实体类存在多对一关系
 * 与Product实体类存在多对一关系
 */
package com.example.teamall.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {
    /** 订单项ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 所属订单，使用JsonBackReference避免循环引用 */
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    /** 关联商品，使用EAGER加载以便及时获取商品信息 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;
    
    /** 商品快照，记录下单时商品的状态 */
    @Column(columnDefinition = "TEXT")
    private String productSnapshot;
    
    /** 商品数量 */
    private Integer quantity;
    
    /** 商品单价 */
    private BigDecimal price;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /**
     * 创建时自动设置创建时间
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 设置订单并维护双向关系
     * @param order 关联的订单
     */
    public void setOrderAndUpdateRelationship(Order order) {
        this.order = order;
        if (order != null && !order.getItems().contains(this)) {
            order.getItems().add(this);
        }
    }
} 
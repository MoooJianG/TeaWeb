/**
 * 商品分类实体类
 * 用于对商品进行分类管理
 * 与Product实体类存在一对多关系
 */
package com.example.teamall.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "categories")
public class Category {
    /** 分类ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 分类名称，不能为空 */
    @Column(nullable = false)
    private String name;
    
    /** 分类描述 */
    private String description;
    
    /** 排序顺序，用于自定义分类的显示顺序 */
    @Column(name = "sort_order")
    private Integer sort;
    
    /** 创建时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 
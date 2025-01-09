/**
 * 收货地址实体类
 * 用于记录用户的收货地址信息
 * 与User实体类存在多对一关系
 */
package com.example.teamall.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "addresses")
public class Address {
    /** 地址ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 所属用户 */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    /** 收货人姓名 */
    private String receiverName;
    
    /** 收货人电话 */
    private String receiverPhone;
    
    /** 省份 */
    private String province;
    
    /** 城市 */
    private String city;
    
    /** 区/县 */
    private String district;
    
    /** 详细地址 */
    private String detailAddress;
    
    /** 是否为默认地址 */
    private Boolean isDefault;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
} 
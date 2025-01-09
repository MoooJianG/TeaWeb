/**
 * 订单状态枚举类
 * 定义订单在系统中的不同状态
 * 用于订单生命周期的管理和流转
 */
package com.example.teamall.model;

public enum OrderStatus {
    /** 待支付状态：订单已创建但未支付 */
    PENDING,
    
    /** 已支付状态：订单已完成支付 */
    PAID,
    
    /** 已发货状态：商品已发出 */
    SHIPPED,
    
    /** 已完成状态：订单已完成交付 */
    COMPLETED,
    
    /** 已取消状态：订单已被取消 */
    CANCELLED
} 
/**
 * 商品状态枚举类
 * 定义商品在系统中的不同状态
 * 用于商品上下架和库存管理
 */
package com.example.teamall.model;

public enum ProductStatus {
    /** 在售状态：商品正常销售中 */
    ON_SALE,
    
    /** 下架状态：商品已下架，不可购买 */
    OFF_SHELF,
    
    /** 售罄状态：商品库存为0 */
    SOLD_OUT
} 
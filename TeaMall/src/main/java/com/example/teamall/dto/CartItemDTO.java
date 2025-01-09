/**
 * 购物车商品项数据传输对象
 * 用于接收添加商品到购物车的请求数据
 */
package com.example.teamall.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    /** 商品ID */
    private Long productId;
    
    /** 商品数量 */
    private Integer quantity;
} 
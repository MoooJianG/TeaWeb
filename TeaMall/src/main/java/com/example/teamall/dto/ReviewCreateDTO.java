/**
 * 商品评价创建数据传输对象
 * 用于接收用户提交的商品评价信息
 */
package com.example.teamall.dto;

import lombok.Data;

@Data
public class ReviewCreateDTO {
    /** 商品ID */
    private Long productId;
    
    /** 订单ID */
    private Long orderId;
    
    /** 评分（1-5星） */
    private Integer rating;
    
    /** 评价内容 */
    private String content;
    
    /** 评价图片URL数组 */
    private String[] images;
} 
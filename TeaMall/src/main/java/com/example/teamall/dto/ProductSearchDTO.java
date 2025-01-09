/**
 * 商品搜索数据传输对象
 * 用于接收商品搜索的条件参数
 */
package com.example.teamall.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSearchDTO {
    /** 搜索关键词 */
    private String keyword;
    
    /** 分类ID */
    private Long categoryId;
    
    /** 最低价格 */
    private BigDecimal minPrice;
    
    /** 最高价格 */
    private BigDecimal maxPrice;
    
    /** 排序字段 */
    private String sortBy;
    
    /** 排序方向（asc/desc） */
    private String sortDirection;
    
    /** 页码 */
    private Integer page;
    
    /** 每页数量 */
    private Integer size;
} 
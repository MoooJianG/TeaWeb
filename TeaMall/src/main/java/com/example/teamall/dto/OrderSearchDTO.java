/**
 * 订单搜索数据传输对象
 * 用于接收订单搜索的条件参数
 */
package com.example.teamall.dto;

import com.example.teamall.model.OrderStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderSearchDTO {
    /** 订单编号 */
    private String orderNo;
    
    /** 订单状态 */
    private OrderStatus status;
    
    /** 开始时间 */
    private LocalDateTime startTime;
    
    /** 结束时间 */
    private LocalDateTime endTime;
    
    /** 页码 */
    private Integer page;
    
    /** 每页数量 */
    private Integer size;
} 
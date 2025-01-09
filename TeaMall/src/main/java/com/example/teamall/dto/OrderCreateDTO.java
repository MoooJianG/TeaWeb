/**
 * 订单创建数据传输对象
 * 用于接收前端创建订单时提交的数据
 */
package com.example.teamall.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.math.BigDecimal;

/**
 * 订单创建DTO
 * 包含创建订单所需的所有信息：
 * - 收货地址ID
 * - 订单商品列表
 */
public class OrderCreateDTO {
    /** 收货地址ID */
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;

    /** 订单商品列表 */
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemDTO> items;

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    /**
     * 订单商品项DTO
     * 包含每个商品的详细信息：
     * - 商品ID
     * - 购买数量
     * - 商品单价
     */
    public static class OrderItemDTO {
        /** 商品ID */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /** 购买数量 */
        @NotNull(message = "商品数量不能为空")
        private Integer quantity;

        /** 商品单价 */
        @NotNull(message = "商品价格不能为空")
        private BigDecimal price;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
} 
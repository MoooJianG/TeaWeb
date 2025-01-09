/**
 * 订单实体类
 * 包含订单的基本信息、商品信息、收货信息和支付信息
 * 与OrderItem实体类存在一对多关系
 */
package com.example.teamall.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    /** 订单ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 订单编号，唯一 */
    @Column(nullable = false, unique = true)
    private String orderNo;
    
    /** 下单用户 */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /** 订单商品列表 */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    /** 订单状态 */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    /** 订单总金额 */
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    /** 创建时间 */
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    /** 过期时间 */
    @Column(nullable = false)
    private LocalDateTime expireTime;
    
    /** 支付时间 */
    private LocalDateTime paymentTime;
    
    /** 支付方式 */
    private String paymentMethod;
    
    /** 快递公司 */
    private String shippingCompany;
    
    /** 快递单号 */
    private String shippingNo;
    
    /** 收货人姓名 */
    @Column(nullable = false)
    private String receiverName;
    
    /** 收货人电话 */
    @Column(nullable = false)
    private String receiverPhone;
    
    /** 收货人省份 */
    @Column(nullable = false)
    private String receiverProvince;
    
    /** 收货人城市 */
    @Column(nullable = false)
    private String receiverCity;
    
    /** 收货人区/县 */
    @Column(nullable = false)
    private String receiverDistrict;
    
    /** 收货人详细地址 */
    @Column(nullable = false)
    private String receiverAddress;
    
    /**
     * 创建时自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderNo() {
        return orderNo;
    }
    
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
    
    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }
    
    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getShippingCompany() {
        return shippingCompany;
    }
    
    public void setShippingCompany(String shippingCompany) {
        this.shippingCompany = shippingCompany;
    }
    
    public String getShippingNo() {
        return shippingNo;
    }
    
    public void setShippingNo(String shippingNo) {
        this.shippingNo = shippingNo;
    }
    
    public String getReceiverName() {
        return receiverName;
    }
    
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
    
    public String getReceiverPhone() {
        return receiverPhone;
    }
    
    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }
    
    public String getReceiverProvince() {
        return receiverProvince;
    }
    
    public void setReceiverProvince(String receiverProvince) {
        this.receiverProvince = receiverProvince;
    }
    
    public String getReceiverCity() {
        return receiverCity;
    }
    
    public void setReceiverCity(String receiverCity) {
        this.receiverCity = receiverCity;
    }
    
    public String getReceiverDistrict() {
        return receiverDistrict;
    }
    
    public void setReceiverDistrict(String receiverDistrict) {
        this.receiverDistrict = receiverDistrict;
    }
    
    public String getReceiverAddress() {
        return receiverAddress;
    }
    
    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }
    
    /**
     * 添加订单项
     * 维护订单项与订单的双向关系
     * @param item 订单项
     */
    public void addOrderItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
    
    /**
     * 移除订单项
     * 解除订单项与订单的双向关系
     * @param item 订单项
     */
    public void removeOrderItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
} 
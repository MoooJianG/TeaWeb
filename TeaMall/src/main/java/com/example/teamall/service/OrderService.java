/**
 * 订单服务类
 * 提供订单相关的业务逻辑处理
 * 包括订单创建、支付、发货、收货等功能
 */
package com.example.teamall.service;

import com.example.teamall.model.*;
import com.example.teamall.repository.OrderRepository;
import com.example.teamall.repository.ProductRepository;
import com.example.teamall.repository.AddressRepository;
import com.example.teamall.dto.OrderCreateDTO;
import com.example.teamall.dto.OrderSearchDTO;
import com.example.teamall.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    
    /** 订单数据访问接口 */
    @Autowired
    private OrderRepository orderRepository;
    
    /** 商品数据访问接口 */
    @Autowired
    private ProductRepository productRepository;
    
    /** JSON对象映射器 */
    @Autowired
    private ObjectMapper objectMapper;
    
    /** 地址数据访问接口 */
    @Autowired
    private AddressRepository addressRepository;
    
    /**
     * 创建订单
     * 包括创建订单基本信息、处理订单项、计算总金额等
     * 使用事务确保数据一致性
     * @param user 下单用户
     * @param orderCreateDTO 订单创建数据
     * @return 创建的订单对象
     * @throws BusinessException 当商品不存在、库存不足等情况时抛出异常
     */
    @Transactional
    public Order createOrder(User user, OrderCreateDTO orderCreateDTO) {
        // 1. 创建订单基本信息
        Order order = new Order();
        order.setUser(user);
        order.setOrderNo(generateOrderNo());
        order.setStatus(OrderStatus.PENDING);
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        
        // 获取地址信息并设置到订单中
        Address address = addressRepository.findById(orderCreateDTO.getAddressId())
            .orElseThrow(() -> new BusinessException("收货地址不存在"));
            
        // 复制地址信息到订单
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverAddress(address.getDetailAddress());
        
        // 2. 处理订单项
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderCreateDTO.OrderItemDTO itemDTO : orderCreateDTO.getItems()) {
            // 使用悲观锁获取商品信息
            Product product = productRepository.findByIdWithLock(itemDTO.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));
            
            // 验证商品数量
            if (itemDTO.getQuantity() <= 0 || itemDTO.getQuantity() > 99) {
                throw new BusinessException("商品数量不合法");
            }
            
            // 检查库存
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new BusinessException("商品库存不足");
            }
            
            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());  // 使用商品当前价格
            
            // 保存商品快照
            try {
                orderItem.setProductSnapshot(objectMapper.writeValueAsString(product));
            } catch (Exception e) {
                throw new BusinessException("创建订单失败");
            }
            
            // 扣减库存
            product.setStock(product.getStock() - itemDTO.getQuantity());
            productRepository.save(product);
            
            // 计算总金额
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(itemDTO.getQuantity())));
            
            // 使用辅助方法添加订单项，处理双向关系
            order.addOrderItem(orderItem);
        }
        
        // 3. 设置订单总金额
        order.setTotalAmount(totalAmount);
        
        // 4. 保存订单（会级联保存订单项）
        return orderRepository.save(order);
    }
    
    /**
     * 根据ID查找订单
     * @param id 订单ID
     * @return 订单对象
     * @throws BusinessException 当订单不存在时抛出异常
     */
    public Order findById(Long id) {
        return orderRepository.findByIdWithItems(id)
            .orElseThrow(() -> new BusinessException("订单不存在"));
    }
    
    /**
     * 查询用户的订单列表
     * @param user 用户对象
     * @param pageable 分页参数
     * @return 订单分页结果
     */
    public Page<Order> findByUser(User user, Pageable pageable) {
        return orderRepository.findByUserId(user.getId(), pageable);
    }
    
    /**
     * 更新订单状态
     * 如果订单被取消，会恢复商品库存
     * @param id 订单ID
     * @param status 新的订单状态
     * @return 更新后的订单对象
     */
    @Transactional
    public Order updateStatus(Long id, OrderStatus status) {
        Order order = findById(id);
        order.setStatus(status);
        
        if (status == OrderStatus.CANCELLED) {
            // 取消订单时恢复库存
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }
        
        return orderRepository.save(order);
    }
    
    /**
     * 生成订单编号
     * 使用时间戳和UUID组合生成唯一订单号
     * @return 订单编号
     */
    private String generateOrderNo() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .format(LocalDateTime.now()) + UUID.randomUUID().toString().substring(0, 6);
    }
    
    /**
     * 搜索订单
     * 支持按订单号、状态、时间范围等条件搜索
     * @param user 用户对象
     * @param searchDTO 搜索条件
     * @param pageable 分页参数
     * @return 订单分页结果
     */
    public Page<Order> searchOrders(User user, OrderSearchDTO searchDTO, Pageable pageable) {
        return orderRepository.findByUserAndConditions(
            user.getId(),
            searchDTO.getOrderNo(),
            searchDTO.getStatus(),
            searchDTO.getStartTime(),
            searchDTO.getEndTime(),
            pageable
        );
    }
    
    /**
     * 取消订单
     * 只能取消待支付的订单
     * @param id 订单ID
     * @param user 用户对象
     * @return 取消后的订单对象
     * @throws BusinessException 当订单状态不正确或无权操作时抛出异常
     */
    @Transactional
    public Order cancelOrder(Long id, User user) {
        Order order = findById(id);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("只能取消待支付订单");
        }
        return updateStatus(id, OrderStatus.CANCELLED);
    }
    
    /**
     * 支付订单
     * 检查订单状态和过期时间，更新支付信息
     * @param id 订单ID
     * @param paymentMethod 支付方式
     * @return 支付后的订单对象
     * @throws BusinessException 当订单状态不正确或已过期时抛出异常
     */
    @Transactional
    public Order payOrder(Long id, String paymentMethod) {
        Order order = findById(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("订单状态错误");
        }
        
        // 检查订单是否过期
        if (order.getExpireTime().isBefore(LocalDateTime.now())) {
            cancelOrder(id, order.getUser());
            throw new BusinessException("订单已过期");
        }
        
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PAID);
        return orderRepository.save(order);
    }
    
    /**
     * 发货
     * 更新订单的物流信息
     * @param id 订单ID
     * @param shippingCompany 快递公司
     * @param shippingNo 快递单号
     * @return 发货后的订单对象
     * @throws BusinessException 当订单状态不正确时抛出异常
     */
    @Transactional
    public Order shipOrder(Long id, String shippingCompany, String shippingNo) {
        Order order = findById(id);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("订单状态错误");
        }
        order.setShippingCompany(shippingCompany);
        order.setShippingNo(shippingNo);
        order.setStatus(OrderStatus.SHIPPED);
        return orderRepository.save(order);
    }
    
    /**
     * 确认收货
     * 将订单状态更新为已完成
     * @param id 订单ID
     * @param user 用户对象
     * @return 完成的订单对象
     * @throws BusinessException 当订单状态不正确或无权操作时抛出异常
     */
    @Transactional
    public Order completeOrder(Long id, User user) {
        Order order = findById(id);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessException("订单状态错误");
        }
        return updateStatus(id, OrderStatus.COMPLETED);
    }
} 
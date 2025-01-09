/**
 * 订单管理控制器
 * 处理订单相关的HTTP请求，包括：
 * - 创建订单
 * - 查询订单
 * - 更新订单状态
 * - 订单搜索
 * - 订单操作（取消、支付、发货、完成）
 * 部分接口仅管理员可访问
 */
package com.example.teamall.controller;

import com.example.teamall.common.Result;
import com.example.teamall.model.Order;
import com.example.teamall.model.User;
import com.example.teamall.model.OrderStatus;
import com.example.teamall.service.OrderService;
import com.example.teamall.service.UserService;
import com.example.teamall.dto.OrderCreateDTO;
import com.example.teamall.dto.OrderSearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    /** 订单服务 */
    @Autowired
    private OrderService orderService;
    
    /** 用户服务 */
    @Autowired
    private UserService userService;
    
    /**
     * 创建新订单
     * @param userDetails 当前登录用户
     * @param orderCreateDTO 订单创建信息
     * @return 创建的订单
     */
    @PostMapping
    public Result<Order> createOrder(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestBody OrderCreateDTO orderCreateDTO) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(orderService.createOrder(user, orderCreateDTO));
    }
    
    /**
     * 获取订单详情
     * 用户只能查看自己的订单，管理员可查看所有订单
     * @param id 订单ID
     * @param userDetails 当前登录用户
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public Result<Order> getOrder(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        Order order = orderService.findById(id);
        if (!order.getUser().getId().equals(user.getId()) && !userService.isAdmin(user)) {
            return Result.error("无权查看此订单");
        }
        return Result.success(order);
    }
    
    /**
     * 获取用户订单列表
     * @param userDetails 当前登录用户
     * @param pageable 分页参数
     * @return 分页的订单列表
     */
    @GetMapping
    public Result<Page<Order>> getOrders(@AuthenticationPrincipal UserDetails userDetails,
                                       Pageable pageable) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(orderService.findByUser(user, pageable));
    }
    
    /**
     * 更新订单状态
     * 仅管理员可访问
     * @param id 订单ID
     * @param status 新状态
     * @return 更新后的订单
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Order> updateStatus(@PathVariable Long id,
                                    @RequestParam OrderStatus status) {
        return Result.success(orderService.updateStatus(id, status));
    }
    
    /**
     * 搜索订单
     * @param userDetails 当前登录用户
     * @param searchDTO 搜索条件
     * @param pageable 分页参数
     * @return 分页的订单列表
     */
    @PostMapping("/search")
    public Result<Page<Order>> searchOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody OrderSearchDTO searchDTO,
            Pageable pageable) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(orderService.searchOrders(user, searchDTO, pageable));
    }
    
    /**
     * 取消订单
     * @param id 订单ID
     * @param userDetails 当前登录用户
     * @return 取消后的订单
     */
    @PostMapping("/{id}/cancel")
    public Result<Order> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(orderService.cancelOrder(id, user));
    }
    
    /**
     * 支付订单
     * @param id 订单ID
     * @param paymentMethod 支付方式
     * @param userDetails 当前登录用户
     * @return 支付后的订单
     */
    @PostMapping("/{id}/pay")
    public Result<Order> payOrder(
            @PathVariable Long id,
            @RequestParam String paymentMethod,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        Order order = orderService.findById(id);
        if (!order.getUser().getId().equals(user.getId())) {
            return Result.error("无权操作此订单");
        }
        return Result.success(orderService.payOrder(id, paymentMethod));
    }
    
    /**
     * 发货
     * 仅管理员可访问
     * @param id 订单ID
     * @param shippingCompany 快递公司
     * @param shippingNo 快递单号
     * @return 发货后的订单
     */
    @PostMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Order> shipOrder(
            @PathVariable Long id,
            @RequestParam String shippingCompany,
            @RequestParam String shippingNo) {
        return Result.success(orderService.shipOrder(id, shippingCompany, shippingNo));
    }
    
    /**
     * 完成订单
     * @param id 订单ID
     * @param userDetails 当前登录用户
     * @return 完成后的订单
     */
    @PostMapping("/{id}/complete")
    public Result<Order> completeOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(orderService.completeOrder(id, user));
    }
} 
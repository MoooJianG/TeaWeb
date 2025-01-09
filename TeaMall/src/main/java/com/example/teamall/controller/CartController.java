/**
 * 购物车管理控制器
 * 处理购物车相关的HTTP请求，包括：
 * - 添加商品到购物车
 * - 更新购物车商品数量
 * - 删除购物车商品
 * - 获取购物车列表
 * - 更新商品选中状态
 * 所有接口都需要用户登录
 */
package com.example.teamall.controller;

import com.example.teamall.common.Result;
import com.example.teamall.model.CartItem;
import com.example.teamall.model.User;
import com.example.teamall.service.CartService;
import com.example.teamall.service.UserService;
import com.example.teamall.dto.CartItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    /** 购物车服务 */
    @Autowired
    private CartService cartService;
    
    /** 用户服务 */
    @Autowired
    private UserService userService;
    
    /**
     * 添加商品到购物车
     * @param userDetails 当前登录用户
     * @param cartItemDTO 购物车商品信息（包含商品ID和数量）
     * @return 添加的购物车项
     */
    @PostMapping
    public Result<CartItem> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CartItemDTO cartItemDTO) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(cartService.addToCart(user, cartItemDTO));
    }
    
    /**
     * 更新购物车商品数量
     * @param id 购物车项ID
     * @param userDetails 当前登录用户
     * @param quantity 新的数量
     * @return 更新后的购物车项
     */
    @PutMapping("/{id}/quantity")
    public Result<CartItem> updateQuantity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Integer quantity) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(cartService.updateQuantity(id, user, quantity));
    }
    
    /**
     * 从购物车中删除商品
     * @param id 购物车项ID
     * @param userDetails 当前登录用户
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        cartService.delete(id, user);
        return Result.success();
    }
    
    /**
     * 获取当前用户的购物车列表
     * @param userDetails 当前登录用户
     * @return 购物车商品列表
     */
    @GetMapping
    public Result<List<CartItem>> getList(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(cartService.findByUser(user));
    }
    
    /**
     * 更新购物车商品的选中状态
     * @param id 购物车项ID
     * @param userDetails 当前登录用户
     * @param selected 是否选中
     * @return 更新后的购物车项
     */
    @PutMapping("/{id}/selected")
    public Result<CartItem> updateSelected(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Boolean selected) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(cartService.updateSelected(id, user, selected));
    }
} 
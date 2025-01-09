/**
 * 商品评价管理控制器
 * 处理商品评价相关的HTTP请求，包括：
 * - 创建评价
 * - 管理员回复评价
 * - 查询商品评价
 * - 查询用户评价历史
 */
package com.example.teamall.controller;

import com.example.teamall.common.Result;
import com.example.teamall.model.Review;
import com.example.teamall.model.User;
import com.example.teamall.service.ReviewService;
import com.example.teamall.service.UserService;
import com.example.teamall.dto.ReviewCreateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    
    /** 评价服务 */
    @Autowired
    private ReviewService reviewService;
    
    /** 用户服务 */
    @Autowired
    private UserService userService;
    
    /**
     * 创建商品评价
     * @param userDetails 当前登录用户
     * @param reviewDTO 评价信息
     * @return 创建的评价
     */
    @PostMapping
    public Result<Review> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ReviewCreateDTO reviewDTO) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(reviewService.createReview(user, reviewDTO));
    }
    
    /**
     * 管理员回复评价
     * 仅管理员可访问
     * @param id 评价ID
     * @param reply 回复内容
     * @return 更新后的评价
     */
    @PostMapping("/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Review> reply(
            @PathVariable Long id,
            @RequestParam String reply) {
        return Result.success(reviewService.reply(id, reply));
    }
    
    /**
     * 获取商品的评价列表
     * @param productId 商品ID
     * @param pageable 分页参数
     * @return 分页的评价列表
     */
    @GetMapping("/product/{productId}")
    public Result<Page<Review>> getProductReviews(
            @PathVariable Long productId,
            Pageable pageable) {
        return Result.success(reviewService.findByProduct(productId, pageable));
    }
    
    /**
     * 获取用户的评价历史
     * @param userDetails 当前登录用户
     * @param pageable 分页参数
     * @return 分页的评价列表
     */
    @GetMapping("/user")
    public Result<Page<Review>> getUserReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(reviewService.findByUser(user.getId(), pageable));
    }
} 
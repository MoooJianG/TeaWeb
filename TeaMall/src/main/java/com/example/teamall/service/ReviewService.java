/**
 * 商品评价服务类
 * 提供评价相关的业务逻辑处理
 * 包括创建评价、商家回复、查询评价等功能
 */
package com.example.teamall.service;

import com.example.teamall.model.*;
import com.example.teamall.repository.ReviewRepository;
import com.example.teamall.repository.OrderRepository;
import com.example.teamall.dto.ReviewCreateDTO;
import com.example.teamall.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    
    /** 评价数据访问接口 */
    @Autowired
    private ReviewRepository reviewRepository;
    
    /** 订单数据访问接口 */
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * 创建商品评价
     * 检查订单状态和是否已评价，创建评价信息
     * @param user 评价用户
     * @param reviewDTO 评价数据
     * @return 创建的评价对象
     * @throws BusinessException 当订单已评价或状态不正确时抛出异常
     */
    @Transactional
    public Review createReview(User user, ReviewCreateDTO reviewDTO) {
        // 检查是否已评价
        if (reviewRepository.existsByUserIdAndOrderId(user.getId(), reviewDTO.getOrderId())) {
            throw new BusinessException("该订单已评价");
        }
        
        // 检查订单状态
        Order order = orderRepository.findById(reviewDTO.getOrderId())
            .orElseThrow(() -> new BusinessException("订单不存在"));
            
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("只能评价已完成的订单");
        }
        
        Review review = new Review();
        review.setUser(user);
        review.setProduct(order.getItems().get(0).getProduct());  // 假设一个订单只有一个商品
        review.setOrder(order);
        review.setRating(reviewDTO.getRating());
        review.setContent(reviewDTO.getContent());
        review.setImages(reviewDTO.getImages());
        
        return reviewRepository.save(review);
    }
    
    /**
     * 商家回复评价
     * @param id 评价ID
     * @param reply 回复内容
     * @return 更新后的评价对象
     * @throws BusinessException 当评价不存在时抛出异常
     */
    @Transactional
    public Review reply(Long id, String reply) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new BusinessException("评价不存在"));
            
        review.setReply(reply);
        return reviewRepository.save(review);
    }
    
    /**
     * 查询商品的评价列表
     * @param productId 商品ID
     * @param pageable 分页参数
     * @return 评价分页结果
     */
    public Page<Review> findByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable);
    }
    
    /**
     * 查询用户的评价列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 评价分页结果
     */
    public Page<Review> findByUser(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable);
    }
} 
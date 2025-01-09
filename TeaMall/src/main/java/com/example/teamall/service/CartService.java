/**
 * 购物车服务类
 * 提供购物车相关的业务逻辑处理
 * 包括添加商品、更新数量、删除商品等功能
 */
package com.example.teamall.service;

import com.example.teamall.model.*;
import com.example.teamall.repository.CartItemRepository;
import com.example.teamall.repository.ProductRepository;
import com.example.teamall.dto.CartItemDTO;
import com.example.teamall.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {
    
    /** 购物车项数据访问接口 */
    @Autowired
    private CartItemRepository cartItemRepository;
    
    /** 商品数据访问接口 */
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * 添加商品到购物车
     * 如果商品已在购物车中，则更新数量
     * @param user 用户对象
     * @param cartItemDTO 购物车项数据
     * @return 添加或更新后的购物车项
     * @throws BusinessException 当商品不存在或库存不足时抛出异常
     */
    @Transactional
    public CartItem addToCart(User user, CartItemDTO cartItemDTO) {
        Product product = productRepository.findById(cartItemDTO.getProductId())
            .orElseThrow(() -> new BusinessException("商品不存在"));
            
        // 检查库存
        if (product.getStock() < cartItemDTO.getQuantity()) {
            throw new BusinessException("商品库存不足");
        }
        
        // 检查是否已在购物车中
        CartItem cartItem = cartItemRepository
            .findByUserIdAndProductId(user.getId(), product.getId())
            .orElse(new CartItem());
            
        if (cartItem.getId() == null) {
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(cartItemDTO.getQuantity());
        } else {
            // 更新数量
            int newQuantity = cartItem.getQuantity() + cartItemDTO.getQuantity();
            if (newQuantity > product.getStock()) {
                throw new BusinessException("商品库存不足");
            }
            cartItem.setQuantity(newQuantity);
        }
        
        return cartItemRepository.save(cartItem);
    }
    
    /**
     * 更新购物车中商品的数量
     * @param id 购物车项ID
     * @param user 用户对象
     * @param quantity 新的数量
     * @return 更新后的购物车项
     * @throws BusinessException 当无权操作或库存不足时抛出异常
     */
    @Transactional
    public CartItem updateQuantity(Long id, User user, Integer quantity) {
        CartItem cartItem = findById(id);
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此购物车");
        }
        
        if (quantity > cartItem.getProduct().getStock()) {
            throw new BusinessException("商品库存不足");
        }
        
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }
    
    /**
     * 从购物车中删除商品
     * @param id 购物车项ID
     * @param user 用户对象
     * @throws BusinessException 当无权操作时抛出异常
     */
    @Transactional
    public void delete(Long id, User user) {
        CartItem cartItem = findById(id);
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此购物车");
        }
        cartItemRepository.delete(cartItem);
    }
    
    /**
     * 根据ID查找购物车项
     * @param id 购物车项ID
     * @return 购物车项对象
     * @throws BusinessException 当购物车项不存在时抛出异常
     */
    public CartItem findById(Long id) {
        return cartItemRepository.findById(id)
            .orElseThrow(() -> new BusinessException("购物车项不存在"));
    }
    
    /**
     * 查询用户的购物车列表
     * @param user 用户对象
     * @return 购物车项列表，按创建时间倒序排序
     */
    public List<CartItem> findByUser(User user) {
        return cartItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
    
    /**
     * 更新购物车项的选中状态
     * @param id 购物车项ID
     * @param user 用户对象
     * @param selected 是否选中
     * @return 更新后的购物车项
     * @throws BusinessException 当无权操作时抛出异常
     */
    @Transactional
    public CartItem updateSelected(Long id, User user, Boolean selected) {
        CartItem cartItem = findById(id);
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此购物车");
        }
        cartItem.setSelected(selected);
        return cartItemRepository.save(cartItem);
    }
}
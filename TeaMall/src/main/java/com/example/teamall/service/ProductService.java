/**
 * 商品服务类
 * 提供商品相关的业务逻辑处理
 * 包括商品的创建、更新、删除、搜索等功能
 */
package com.example.teamall.service;

import com.example.teamall.model.Product;
import com.example.teamall.model.ProductStatus;
import com.example.teamall.model.Category;
import com.example.teamall.repository.ProductRepository;
import com.example.teamall.repository.CategoryRepository;
import com.example.teamall.dto.ProductSearchDTO;
import com.example.teamall.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProductService {
    
    /** 商品数据访问接口 */
    @Autowired
    private ProductRepository productRepository;
    
    /** 分类数据访问接口 */
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * 创建商品
     * 设置商品的默认状态和创建时间
     * @param product 商品对象
     * @return 创建后的商品对象
     * @throws BusinessException 当商品信息验证失败时抛出异常
     */
    @Transactional
    public Product create(Product product) {
        validateProduct(product);
        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ON_SALE);
        }
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }
    
    /**
     * 更新商品信息
     * 更新商品的基本信息和更新时间
     * @param id 商品ID
     * @param product 更新的商品信息
     * @return 更新后的商品对象
     * @throws BusinessException 当商品不存在或信息验证失败时抛出异常
     */
    @Transactional
    public Product update(Long id, Product product) {
        Product existingProduct = productRepository.findById(id)
            .orElseThrow(() -> new BusinessException("商品不存在"));
        
        validateProduct(product);
        
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setStatus(product.getStatus());
        existingProduct.setImageUrl(product.getImageUrl());
        existingProduct.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(existingProduct);
    }
    
    /**
     * 删除商品
     * @param id 商品ID
     * @throws BusinessException 当商品不存在时抛出异常
     */
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException("商品不存在");
        }
        productRepository.deleteById(id);
    }
    
    /**
     * 根据ID查找商品
     * @param id 商品ID
     * @return 商品对象
     * @throws BusinessException 当商品不存在时抛出异常
     */
    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new BusinessException("商品不存在"));
    }
    
    /**
     * 搜索商品
     * 支持分页、排序和多条件搜索
     * @param searchDTO 搜索条件
     * @return 商品分页结果
     */
    public Page<Product> search(ProductSearchDTO searchDTO) {
        PageRequest pageRequest = PageRequest.of(
            searchDTO.getPage() != null ? searchDTO.getPage() : 0,
            searchDTO.getSize() != null ? searchDTO.getSize() : 10,
            Sort.by(Sort.Direction.fromString(
                searchDTO.getSortDirection() != null ? searchDTO.getSortDirection() : "DESC"),
                searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "id")
        );
        
        return productRepository.search(
            searchDTO.getCategoryId(),
            searchDTO.getKeyword(),
            searchDTO.getMinPrice(),
            searchDTO.getMaxPrice(),
            pageRequest
        );
    }
    
    /**
     * 验证商品信息
     * 检查商品的分类、名称、价格、库存等信息是否合法
     * @param product 待验证的商品对象
     * @throws BusinessException 当验证失败时抛出异常
     */
    private void validateProduct(Product product) {
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new BusinessException("分类不存在"));
            product.setCategory(category);
        }
        
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new BusinessException("商品名称不能为空");
        }
        
        if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("商品价格必须大于0");
        }
        
        if (product.getStock() == null || product.getStock() < 0) {
            throw new BusinessException("商品库存不能为负数");
        }
    }
} 
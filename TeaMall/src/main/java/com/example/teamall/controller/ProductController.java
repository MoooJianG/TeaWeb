/**
 * 商品管理控制器
 * 处理商品相关的HTTP请求，包括：
 * - 商品的增删改查
 * - 商品搜索
 * 其中管理员专用接口包括：创建、更新、删除商品
 */
package com.example.teamall.controller;

import com.example.teamall.common.Result;
import com.example.teamall.model.Product;
import com.example.teamall.service.ProductService;
import com.example.teamall.dto.ProductSearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    /** 商品服务 */
    @Autowired
    private ProductService productService;
    
    /**
     * 创建新商品
     * 仅管理员可访问
     * @param product 商品信息
     * @return 创建的商品信息
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Product> create(@RequestBody Product product) {
        return Result.success(productService.create(product));
    }
    
    /**
     * 更新商品信息
     * 仅管理员可访问
     * @param id 商品ID
     * @param product 更新的商品信息
     * @return 更新后的商品信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return Result.success(productService.update(id, product));
    }
    
    /**
     * 删除商品
     * 仅管理员可访问
     * @param id 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
    
    /**
     * 根据ID查询商品
     * 所有用户可访问
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    public Result<Product> findById(@PathVariable Long id) {
        return Result.success(productService.findById(id));
    }
    
    /**
     * 搜索商品
     * 支持分页和条件查询
     * 所有用户可访问
     * @param searchDTO 搜索条件
     * @return 分页的商品列表
     */
    @GetMapping
    public Result<Page<Product>> search(ProductSearchDTO searchDTO) {
        return Result.success(productService.search(searchDTO));
    }
} 
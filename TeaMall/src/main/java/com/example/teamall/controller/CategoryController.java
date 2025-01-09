/**
 * 商品分类管理控制器
 * 处理商品分类相关的HTTP请求，包括：
 * - 分类的增删改查
 * - 获取所有分类列表
 * 其中管理员专用接口包括：创建、更新、删除分类
 */
package com.example.teamall.controller;

import com.example.teamall.common.Result;
import com.example.teamall.model.Category;
import com.example.teamall.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    /** 分类服务 */
    @Autowired
    private CategoryService categoryService;

    /**
     * 创建新分类
     * 仅管理员可访问
     * @param category 分类信息
     * @return 创建的分类信息
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> create(@RequestBody Category category) {
        return Result.success(categoryService.create(category));
    }

    /**
     * 更新分类信息
     * 仅管理员可访问
     * @param id 分类ID
     * @param category 更新的分类信息
     * @return 更新后的分类信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category category) {
        return Result.success(categoryService.update(id, category));
    }

    /**
     * 删除分类
     * 仅管理员可访问
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    /**
     * 根据ID查询分类
     * 所有用户可访问
     * @param id 分类ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    public Result<Category> findById(@PathVariable Long id) {
        return Result.success(categoryService.findById(id));
    }

    /**
     * 获取所有分类列表
     * 所有用户可访问
     * @return 分类列表
     */
    @GetMapping
    public Result<List<Category>> findAll() {
        return Result.success(categoryService.findAll());
    }
} 
/**
 * 商品分类服务类
 * 提供分类相关的业务逻辑处理
 * 包括分类的创建、更新、删除、查询等功能
 */
package com.example.teamall.service;

import com.example.teamall.model.Category;
import com.example.teamall.repository.CategoryRepository;
import com.example.teamall.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {
    
    /** 分类数据访问接口 */
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * 创建商品分类
     * 检查分类名称是否重复，设置创建时间
     * @param category 分类对象
     * @return 创建的分类对象
     * @throws BusinessException 当分类名称为空或已存在时抛出异常
     */
    @Transactional
    public Category create(Category category) {
        validateCategory(category);
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new BusinessException("分类名称已存在");
        }
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }
    
    /**
     * 更新商品分类
     * 检查分类是否存在，名称是否重复
     * @param id 分类ID
     * @param category 更新的分类信息
     * @return 更新后的分类对象
     * @throws BusinessException 当分类不存在、名称为空或已存在时抛出异常
     */
    @Transactional
    public Category update(Long id, Category category) {
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new BusinessException("分类不存在"));
        
        validateCategory(category);
        
        String newName = category.getName();
        if (newName != null && !newName.equals(existingCategory.getName()) && 
            categoryRepository.findByName(newName)
                .filter(c -> !c.getId().equals(id))
                .isPresent()) {
            throw new BusinessException("分类名称已存在");
        }
        
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setSort(category.getSort());
        existingCategory.setUpdatedAt(LocalDateTime.now());
        
        return categoryRepository.save(existingCategory);
    }
    
    /**
     * 删除商品分类
     * @param id 分类ID
     * @throws BusinessException 当分类不存在时抛出异常
     */
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException("分类不存在");
        }
        categoryRepository.deleteById(id);
    }
    
    /**
     * 根据ID查找分类
     * @param id 分类ID
     * @return 分类对象
     * @throws BusinessException 当分类不存在时抛出异常
     */
    public Category findById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new BusinessException("分类不存在"));
    }
    
    /**
     * 查询所有分类
     * @return 分类列表
     */
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
    
    /**
     * 验证分类信息
     * 检查分类名称是否为空
     * @param category 待验证的分类对象
     * @throws BusinessException 当分类名称为空时抛出异常
     */
    private void validateCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BusinessException("分类名称不能为空");
        }
    }
} 
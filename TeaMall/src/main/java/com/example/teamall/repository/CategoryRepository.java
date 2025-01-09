/**
 * 商品分类数据访问接口
 * 提供对Category实体的基本CRUD操作和自定义查询方法
 * 继承JpaRepository以获取基本的数据库操作功能
 */
package com.example.teamall.repository;

import com.example.teamall.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /**
     * 检查分类名称是否已存在
     * @param name 分类名称
     * @return true表示名称已存在，false表示名称可用
     */
    boolean existsByName(String name);

    /**
     * 根据分类名称查找分类
     * @param name 分类名称
     * @return 包含分类信息的Optional对象
     */
    Optional<Category> findByName(String name);
} 
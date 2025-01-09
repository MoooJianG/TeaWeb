/**
 * 收货地址数据访问接口
 * 提供对Address实体的基本CRUD操作和自定义查询方法
 * 继承JpaRepository以获取基本的数据库操作功能
 */
package com.example.teamall.repository;

import com.example.teamall.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    /**
     * 查询用户的收货地址列表
     * 默认地址优先，其次按创建时间倒序排序
     * @param userId 用户ID
     * @return 收货地址列表
     */
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    /**
     * 清除用户其他地址的默认标记
     * 用于设置新的默认地址时，确保只有一个默认地址
     * @param userId 用户ID
     * @param addressId 新的默认地址ID
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.id != :addressId")
    void clearOtherDefaultAddress(Long userId, Long addressId);
} 
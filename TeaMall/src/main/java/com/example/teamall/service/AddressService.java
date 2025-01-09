/**
 * 收货地址服务类
 * 提供收货地址相关的业务逻辑处理
 * 包括地址的创建、更新、删除、设置默认地址等功能
 */
package com.example.teamall.service;

import com.example.teamall.model.Address;
import com.example.teamall.model.User;
import com.example.teamall.repository.AddressRepository;
import com.example.teamall.dto.AddressDTO;
import com.example.teamall.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddressService {
    
    /** 地址数据访问接口 */
    @Autowired
    private AddressRepository addressRepository;
    
    /**
     * 创建收货地址
     * 如果设置为默认地址，会清除用户其他地址的默认标记
     * @param user 用户对象
     * @param addressDTO 地址数据
     * @return 创建的地址对象
     */
    @Transactional
    public Address create(User user, AddressDTO addressDTO) {
        Address address = new Address();
        address.setUser(user);
        updateAddressFromDTO(address, addressDTO);
        address.setCreatedAt(LocalDateTime.now());
        
        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            addressRepository.clearOtherDefaultAddress(user.getId(), null);
        }
        
        return addressRepository.save(address);
    }
    
    /**
     * 更新收货地址
     * 如果设置为默认地址，会清除用户其他地址的默认标记
     * @param id 地址ID
     * @param user 用户对象
     * @param addressDTO 更新的地址数据
     * @return 更新后的地址对象
     * @throws BusinessException 当无权修改时抛出异常
     */
    @Transactional
    public Address update(Long id, User user, AddressDTO addressDTO) {
        Address address = findById(id);
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权修改此地址");
        }
        
        updateAddressFromDTO(address, addressDTO);
        
        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            addressRepository.clearOtherDefaultAddress(user.getId(), id);
        }
        
        return addressRepository.save(address);
    }
    
    /**
     * 删除收货地址
     * @param id 地址ID
     * @param user 用户对象
     * @throws BusinessException 当无权删除时抛出异常
     */
    @Transactional
    public void delete(Long id, User user) {
        Address address = findById(id);
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权删除此地址");
        }
        addressRepository.delete(address);
    }
    
    /**
     * 根据ID查找地址
     * @param id 地址ID
     * @return 地址对象
     * @throws BusinessException 当地址不存在时抛出异常
     */
    public Address findById(Long id) {
        return addressRepository.findById(id)
            .orElseThrow(() -> new BusinessException("地址不存在"));
    }
    
    /**
     * 查询用户的地址列表
     * 默认地址优先，其次按创建时间倒序排序
     * @param user 用户对象
     * @return 地址列表
     */
    public List<Address> findByUser(User user) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
    }
    
    /**
     * 设置默认地址
     * 会清除用户其他地址的默认标记
     * @param id 地址ID
     * @param user 用户对象
     * @return 设置为默认的地址对象
     * @throws BusinessException 当无权操作时抛出异常
     */
    @Transactional
    public Address setDefault(Long id, User user) {
        Address address = findById(id);
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权操作此地址");
        }
        
        addressRepository.clearOtherDefaultAddress(user.getId(), id);
        address.setIsDefault(true);
        return addressRepository.save(address);
    }
    
    /**
     * 从DTO更新地址信息
     * 用于创建和更新地址时复用代码
     * @param address 待更新的地址对象
     * @param addressDTO 地址数据
     */
    private void updateAddressFromDTO(Address address, AddressDTO addressDTO) {
        address.setReceiverName(addressDTO.getReceiverName());
        address.setReceiverPhone(addressDTO.getReceiverPhone());
        address.setProvince(addressDTO.getProvince());
        address.setCity(addressDTO.getCity());
        address.setDistrict(addressDTO.getDistrict());
        address.setDetailAddress(addressDTO.getDetailAddress());
        address.setIsDefault(addressDTO.getIsDefault());
    }
} 
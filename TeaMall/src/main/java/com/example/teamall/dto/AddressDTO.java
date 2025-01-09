/**
 * 收货地址数据传输对象
 * 用于接收和返回收货地址相关的数据
 */
package com.example.teamall.dto;

import lombok.Data;

@Data
public class AddressDTO {
    /** 收货人姓名 */
    private String receiverName;
    
    /** 收货人电话 */
    private String receiverPhone;
    
    /** 省份 */
    private String province;
    
    /** 城市 */
    private String city;
    
    /** 区/县 */
    private String district;
    
    /** 详细地址 */
    private String detailAddress;
    
    /** 是否为默认地址 */
    private Boolean isDefault;
} 
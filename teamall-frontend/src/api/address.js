import request from '@/utils/request'

// 获取收货地址列表
export function getAddressList() {
  return request({
    url: '/addresses',
    method: 'get'
  })
}

// 添加收货地址
export function addAddress(data) {
  return request({
    url: '/addresses',
    method: 'post',
    data
  })
}

// 更新收货地址
export function updateAddress(id, data) {
  return request({
    url: `/addresses/${id}`,
    method: 'put',
    data
  })
}

// 删除收货地址
export function deleteAddress(id) {
  return request({
    url: `/addresses/${id}`,
    method: 'delete'
  })
}

// 设置默认地址
export function setDefaultAddress(id) {
  return request({
    url: `/addresses/${id}/default`,
    method: 'put'
  })
} 
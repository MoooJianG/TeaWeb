import request from '@/utils/request'

// 获取订单列表
export function getOrders() {
  return request({
    url: '/orders',
    method: 'get'
  })
}

// 搜索订单
export function searchOrders(data) {
  return request({
    url: '/orders/search',
    method: 'post',
    data
  })
}

// 获取订单详情
export function getOrder(id) {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  })
}

// 创建订单
export function createOrder(data) {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

// 支付订单
export function payOrder(id, paymentMethod = 'ALIPAY') {
  return request({
    url: `/orders/${id}/pay`,
    method: 'post',
    params: {
      paymentMethod
    }
  })
}

// 取消订单
export function cancelOrder(id) {
  return request({
    url: `/orders/${id}/cancel`,
    method: 'post'
  })
}

// 确认收货
export function completeOrder(id) {
  return request({
    url: `/orders/${id}/complete`,
    method: 'post'
  })
}

// 删除订单（逻辑删除）
export function deleteOrder(id) {
  return request({
    url: `/orders/${id}`,
    method: 'delete'
  })
}

// 更新订单状态
export function updateOrderStatus(id, status) {
  return request({
    url: `/orders/${id}/status`,
    method: 'put',
    params: { status }
  })
} 
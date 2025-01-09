import request from '@/utils/request'

// 获取购物车列表
export function getCartItems() {
  return request({
    url: '/cart',
    method: 'get'
  })
}

// 添加商品到购物车
export function addToCart(data) {
  return request({
    url: '/cart',
    method: 'post',
    data
  })
}

// 更新购物车商品数量
export function updateCartItem(id, quantity) {
  return request({
    url: `/cart/${id}/quantity`,
    method: 'put',
    params: { quantity }
  })
}

// 删除购物车商品
export function removeFromCart(id) {
  return request({
    url: `/cart/${id}`,
    method: 'delete'
  })
}

// 更新商品选中状态
export function updateCartItemSelected(id, selected) {
  return request({
    url: `/cart/${id}/selected`,
    method: 'put',
    params: { selected }
  })
} 
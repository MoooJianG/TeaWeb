import request from '@/utils/request'

// 获取商品评价列表
export function getProductReviews(productId, params) {
  return request({
    url: `/reviews/product/${productId}`,
    method: 'get',
    params
  })
}

// 创建评价
export function createReview(data) {
  return request({
    url: '/reviews',
    method: 'post',
    data
  })
}

// 获取用户评价列表
export function getUserReviews(params) {
  return request({
    url: '/reviews/user',
    method: 'get',
    params
  })
}

// 回复评价
export function replyReview(id, reply) {
  return request({
    url: `/reviews/${id}/reply`,
    method: 'post',
    params: { reply }
  })
} 
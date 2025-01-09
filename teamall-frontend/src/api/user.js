import request from '@/utils/request'

// 用户注册
export function register(data) {
  return request({
    url: '/users/register',
    method: 'post',
    data
  })
}

// 用户登录
export function login(data) {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/users/info',
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/users/info',
    method: 'put',
    data
  })
}

// 更新密码
export function updatePassword(data) {
  return request({
    url: '/users/password',
    method: 'put',
    params: data
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/users/logout',
    method: 'post'
  })
} 
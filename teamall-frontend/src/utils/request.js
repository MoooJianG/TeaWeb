import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const token = userStore.getToken
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 打印响应信息，用于调试
    console.log('Response interceptor:', {
      url: response.config.url,
      method: response.config.method,
      status: response.status,
      data: res,
      headers: response.headers
    })
    
    // 处理成功响应
    if (res.code === 200) {
      // 如果响应中包含token，更新它
      if (res.data?.token) {
        console.log('Token found in response:', res.data.token)
        const userStore = useUserStore()
        userStore.setToken(res.data.token)
        console.log('Token updated in store:', userStore.getToken)
      }
      return res
    }
    
    // 处理业务错误
    console.error('Business error:', res.message)
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message || '操作失败'))
  },
  error => {
    console.error('Response error:', {
      url: error.config?.url,
      method: error.config?.method,
      status: error.response?.status,
      data: error.response?.data,
      message: error.message
    })
    
    // 处理特定的错误状态
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，清除用户信息并跳转到登录页
          console.log('Unauthorized, logging out...')
          const userStore = useUserStore()
          userStore.logout()
          router.push({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath }
          })
          break
        case 403:
          ElMessage.error('没有权限进行此操作')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误，请稍后重试')
          break
        default:
          ElMessage.error(error.response.data?.message || '网络错误，请稍后重试')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络设置')
    } else {
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default request 
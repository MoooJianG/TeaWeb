import { defineStore } from 'pinia'
import { getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => {
    let userInfo = {}
    let token = ''
    
    try {
      const storedToken = localStorage.getItem('token')
      const storedUserInfo = localStorage.getItem('userInfo')
      
      if (storedToken && storedToken !== 'undefined' && storedToken !== 'null') {
        token = storedToken
        
        if (storedUserInfo && storedUserInfo !== 'undefined' && storedUserInfo !== 'null') {
          try {
            userInfo = JSON.parse(storedUserInfo)
          } catch (e) {
            console.error('Failed to parse stored user info:', e)
            localStorage.removeItem('userInfo')
          }
        }
      } else {
        // 如果token无效，清除所有存储的数据
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      }
    } catch (error) {
      console.error('Error initializing user store:', error)
      // 如果出错，清除可能损坏的数据
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
    
    return {
      token,
      userInfo
    }
  },
  
  getters: {
    isLoggedIn: (state) => {
      return !!(state.token && state.token !== 'undefined' && state.token !== 'null')
    },
    getToken: (state) => state.token,
    getUserInfo: (state) => state.userInfo || {},
    isAdmin: (state) => state.userInfo?.role === 'ROLE_ADMIN'
  },
  
  actions: {
    setToken(token) {
      if (!token || token === 'undefined' || token === 'null') {
        this.token = ''
        localStorage.removeItem('token')
        return
      }
      
      this.token = token
      localStorage.setItem('token', token)
      console.log('Token stored:', token)
    },
    
    setUserInfo(userInfo) {
      if (!userInfo) {
        this.userInfo = {}
        localStorage.removeItem('userInfo')
        return
      }
      
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      console.log('User info stored:', userInfo)
    },
    
    async refreshUserInfo() {
      if (!this.isLoggedIn) {
        this.setUserInfo(null)
        return null
      }
      
      try {
        const response = await getUserInfo()
        if (response.code === 200 && response.data) {
          this.setUserInfo(response.data)
          return response.data
        }
        return null
      } catch (error) {
        console.error('Failed to refresh user info:', error)
        if (error.response?.status === 401) {
          this.logout()
        }
        return null
      }
    },
    
    async logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      console.log('Logged out, token and user info cleared')
    },
    
    // 检查并恢复登录状态
    async checkLoginState() {
      if (!this.isLoggedIn) {
        console.log('No valid token found')
        return false
      }
      
      try {
        const userInfo = await this.refreshUserInfo()
        console.log('Login state checked:', !!userInfo)
        return !!userInfo
      } catch (error) {
        console.error('Failed to check login state:', error)
        if (error.response?.status === 401) {
          this.logout()
        }
        return false
      }
    }
  }
}) 
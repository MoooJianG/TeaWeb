<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>登录茶韵商城</h2>
        <p>欢迎回来，请登录您的账号</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="loginForm.email"
            placeholder="请输入邮箱"
          >
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <div class="remember-forgot">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-button link type="primary" @click="forgotPassword">忘记密码？</el-button>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="submit-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="register-link">
          还没有账号？
          <el-button link type="primary" @click="$router.push('/register')">
            立即注册
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { login } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref(null)
const rememberMe = ref(false)
const loading = ref(false)

const loginForm = reactive({
  email: '',
  password: ''
})

// 表单验证规则
const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在 6 到 20 个字符之间', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    console.log('Sending login request...')
    const response = await login({
      email: loginForm.email,
      password: loginForm.password
    })
    
    console.log('Login response:', response)
    
    if (response.code === 200 && response.data) {
      const { token, user } = response.data
      
      console.log('Received token:', token)
      console.log('Received user:', user)
      
      if (!token) {
        console.error('No token in response:', response)
        throw new Error('登录失败：未获取到token')
      }
      
      // 确保先设置token
      userStore.setToken(token)
      console.log('Token set in store:', userStore.getToken)
      
      // 设置用户信息
      userStore.setUserInfo(user)
      console.log('User info set in store:', userStore.getUserInfo)
      
      // 验证登录状态
      const isLoggedIn = userStore.isLoggedIn
      console.log('Login state after setting token:', isLoggedIn)
      
      // 如果记住我，保存邮箱
      if (rememberMe.value) {
        localStorage.setItem('remember_email', loginForm.email)
      } else {
        localStorage.removeItem('remember_email')
      }
      
      ElMessage.success('登录成功')
      
      // 如果有重定向地址，则跳转到重定向地址
      const redirect = route.query.redirect || '/'
      console.log('Redirecting to:', redirect)
      router.push(redirect)
    } else {
      console.error('Login failed:', response)
      throw new Error(response.message || '登录失败')
    }
  } catch (error) {
    console.error('Login error:', error)
    ElMessage.error(error.message || '登录失败，请重试')
    // 登录失败时清除可能的token
    userStore.logout()
  } finally {
    loading.value = false
  }
}

// 在组件挂载时检查是否有记住的邮箱
onMounted(() => {
  const rememberedEmail = localStorage.getItem('remember_email')
  if (rememberedEmail) {
    loginForm.email = rememberedEmail
    rememberMe.value = true
  }
})

// 忘记密码
const forgotPassword = () => {
  ElMessage.info('忘记密码功能开发中...')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #18624b;
  margin-bottom: 10px;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

@media (max-width: 480px) {
  .login-box {
    width: 90%;
    padding: 20px;
  }
}
</style> 
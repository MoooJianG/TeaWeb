<template>
  <div class="app-wrapper">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <router-link to="/">
            <h1>茶韵商城</h1>
          </router-link>
        </div>
        
        <el-menu
          mode="horizontal"
          :router="true"
          class="nav-menu"
          :ellipsis="false"
          :default-active="activeMenu"
        >
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            首页
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><Shop /></el-icon>
            茶品商城
          </el-menu-item>
          <el-menu-item index="/admin/products" v-if="isAdmin">
            <el-icon><Goods /></el-icon>
            商品管理
          </el-menu-item>
          <el-menu-item index="/cart" v-if="isLoggedIn">
            <el-icon><ShoppingCart /></el-icon>
            购物车
            <el-badge v-if="cartItemCount > 0" :value="cartItemCount" class="cart-badge" />
          </el-menu-item>
        </el-menu>

        <div class="user-actions">
          <template v-if="!isLoggedIn">
            <el-button type="text" @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
          <template v-else>
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                {{ username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="user">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-container">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <p>© 2024 茶韵商城 版权所有</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, Shop, ShoppingCart, ArrowDown, Goods } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getCartItems } from '@/api/cart'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 使用 store 中的状态
const isLoggedIn = computed(() => userStore.isLoggedIn)
const username = computed(() => {
  const userInfo = userStore.getUserInfo
  console.log('Current userInfo:', userInfo)
  // 优先显示用户名，如果没有用户名则显示邮箱
  if (userInfo?.username && userInfo.username.trim() !== '') {
    return userInfo.username
  }
  // 如果没有用户名，显示邮箱的@前面部分
  if (userInfo?.email) {
    return userInfo.email.split('@')[0]
  }
  return '用户'
})

// 购物车商品数量
const cartItemCount = ref(0)

// 当前激活的菜单项
const activeMenu = computed(() => {
  if (route.path === '/') return '/'
  const mainPath = '/' + route.path.split('/')[1]
  return mainPath
})

// 获取购物车商品数量
const updateCartCount = async () => {
  if (isLoggedIn.value) {
    try {
      const response = await getCartItems()
      if (response.code === 200) {
        cartItemCount.value = response.data.length
      }
    } catch (error) {
      console.error('Failed to get cart items:', error)
    }
  } else {
    cartItemCount.value = 0
  }
}

// 处理用户菜单命令
const handleCommand = async (command) => {
  switch (command) {
    case 'user':
      router.push('/user')
      break
    case 'orders':
      router.push('/order/list')
      break
    case 'logout':
      await userStore.logout()
      cartItemCount.value = 0
      ElMessage.success('退出登录成功')
      router.push('/login')
      break
  }
}

// 监听登录状态变化
watch(() => userStore.isLoggedIn, (newValue) => {
  console.log('Login state changed:', newValue)
  if (newValue) {
    updateCartCount()
  } else {
    cartItemCount.value = 0
  }
}, { immediate: true })

// 监听用户信息变化
watch(() => userStore.getUserInfo, (newValue) => {
  console.log('User info changed:', newValue)
}, { deep: true })

// 判断是否为管理员
const isAdmin = computed(() => userStore.isAdmin)

onMounted(async () => {
  // 如果已登录，获取购物车数量和刷新用户信息
  if (isLoggedIn.value) {
    await Promise.all([
      updateCartCount(),
      userStore.refreshUserInfo()
    ])
  }
})
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  width: 100%;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.logo {
  margin-right: 40px;
}

.logo a {
  text-decoration: none;
  color: #18624b;
}

.logo h1 {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.nav-menu :deep(.el-menu-item) {
  font-size: 16px;
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: #18624b;
  border-bottom-color: #18624b;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #18624b;
}

.main-container {
  flex: 1;
  margin-top: 60px;
  padding: 20px;
  max-width: 1200px;
  width: 100%;
  margin-left: auto;
  margin-right: auto;
}

.footer {
  background: #f5f5f5;
  padding: 20px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  color: #666;
}

.cart-badge {
  margin-top: -8px;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 10px;
  }
  
  .logo {
    margin-right: 20px;
  }
  
  .logo h1 {
    font-size: 20px;
  }
  
  .nav-menu :deep(.el-menu-item) {
    padding: 0 10px;
    font-size: 14px;
  }
}
</style> 
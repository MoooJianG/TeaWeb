<!--
  主布局组件
  包含页面的整体结构：顶部导航栏、主要内容区域和页脚
-->
<template>
  <div class="app-wrapper">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <!-- Logo和网站名称 -->
        <div class="logo">
          <router-link to="/">
            <img src="@/assets/logo.png" alt="TeaMall Logo">
            <span class="title">TeaMall</span>
          </router-link>
        </div>

        <!-- 主导航菜单 -->
        <nav class="nav-menu">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/products" class="nav-item">商品</router-link>
          <router-link v-if="isAdmin" to="/admin" class="nav-item">管理后台</router-link>
        </nav>

        <!-- 用户操作区域 -->
        <div class="user-actions">
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #suffix>
                <el-icon class="search-icon" @click="handleSearch">
                  <Search />
                </el-icon>
              </template>
            </el-input>
          </div>

          <!-- 购物车入口 -->
          <router-link to="/cart" class="cart-link">
            <el-badge :value="cartCount" :hidden="!cartCount">
              <el-icon><ShoppingCart /></el-icon>
            </el-badge>
          </router-link>

          <!-- 用户菜单 -->
          <div v-if="isLoggedIn" class="user-menu">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userInfo?.avatar">
                  {{ userInfo?.nickname?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ userInfo?.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="address">收货地址</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <!-- 登录注册入口 -->
          <div v-else class="auth-links">
            <router-link to="/login" class="nav-item">登录</router-link>
            <router-link to="/register" class="nav-item">注册</router-link>
          </div>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <p class="copyright">&copy; {{ currentYear }} TeaMall. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ShoppingCart } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

// 搜索关键词
const searchKeyword = ref('')

// 计算属性
/**
 * 获取当前年份
 * @returns {number} 当前年份
 */
const currentYear = computed(() => new Date().getFullYear())

/**
 * 判断用户是否已登录
 * @returns {boolean} 登录状态
 */
const isLoggedIn = computed(() => userStore.isLoggedIn)

/**
 * 获取用户信息
 * @returns {Object|null} 用户信息对象
 */
const userInfo = computed(() => userStore.userInfo)

/**
 * 判断是否为管理员
 * @returns {boolean} 管理员状态
 */
const isAdmin = computed(() => userStore.roles.includes('ROLE_ADMIN'))

/**
 * 获取购物车商品数量
 * @returns {number} 商品数量
 */
const cartCount = computed(() => cartStore.totalCount)

// 方法定义
/**
 * 处理搜索
 * 跳转到商品列表页并带上搜索关键词
 */
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/products',
      query: { keyword: searchKeyword.value.trim() }
    })
  }
}

/**
 * 处理用户菜单命令
 * @param {string} command - 菜单命令
 */
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'address':
      router.push('/address')
      break
    case 'logout':
      await userStore.logoutUser()
      ElMessage.success('已退出登录')
      router.push('/login')
      break
  }
}

// 生命周期钩子
onMounted(async () => {
  // 如果已登录，加载用户信息和购物车数据
  if (isLoggedIn.value) {
    await Promise.all([
      userStore.loadUserInfo(),
      cartStore.loadItems()
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
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
}

.logo img {
  height: 40px;
  margin-right: 10px;
}

.title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.nav-menu {
  display: flex;
  gap: 20px;
}

.nav-item {
  color: #333;
  text-decoration: none;
  font-size: 16px;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-item:hover {
  background-color: #f5f5f5;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-box {
  width: 200px;
}

.cart-link {
  color: #333;
  font-size: 20px;
}

.user-menu {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-content {
  flex: 1;
  margin-top: 60px;
  padding: 20px;
  max-width: 1200px;
  width: 100%;
  margin-left: auto;
  margin-right: auto;
}

.footer {
  background-color: #f5f5f5;
  padding: 20px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
}

.copyright {
  color: #666;
  font-size: 14px;
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 
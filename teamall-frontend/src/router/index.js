import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import Layout from '@/layout/Layout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: Layout,
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/home/index.vue')
        },
        {
          path: 'products',
          name: 'products',
          component: () => import('@/views/products/index.vue')
        },
        {
          path: 'product/:id',
          name: 'product-detail',
          component: () => import('@/views/product/detail.vue')
        },
        {
          path: 'user',
          name: 'user',
          component: () => import('@/views/user/index.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'cart',
          name: 'cart',
          component: () => import('@/views/cart/index.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'orders',
          redirect: '/order/list'
        },
        {
          path: 'addresses',
          name: 'addresses',
          component: () => import('@/views/address/index.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'admin/products',
          name: 'admin-products',
          component: () => import('@/views/admin/products/index.vue'),
          meta: { requiresAuth: true, requiresAdmin: true }
        }
      ]
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/register/index.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/order',
      component: Layout,
      children: [
        {
          path: 'list',
          name: 'OrderList',
          component: () => import('@/views/order/list.vue'),
          meta: { title: '我的订单', requiresAuth: true }
        },
        {
          path: 'settlement',
          name: 'OrderSettlement',
          component: () => import('@/views/order/settlement.vue'),
          meta: { title: '订单结算', requiresAuth: true }
        },
        {
          path: 'detail/:id',
          name: 'OrderDetail',
          component: () => import('@/views/order/detail.vue'),
          meta: { title: '订单详情', requiresAuth: true }
        }
      ]
    },
    {
      path: '/review',
      component: Layout,
      children: [
        {
          path: 'create/:id',
          name: 'ReviewCreate',
          component: () => import('@/views/review/create.vue'),
          meta: { title: '评价商品', requiresAuth: true }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn
  const isAdmin = userStore.isAdmin

  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && !isAdmin) {
    ElMessage.error('需要管理员权限')
    next('/')
    return
  }

  // 需要登录的页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 已登录用户不能访问登录/注册页
  if (to.meta.requiresGuest && isLoggedIn) {
    next('/')
    return
  }

  next()
})

export default router 
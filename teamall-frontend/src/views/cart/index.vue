<template>
  <div class="cart-container" v-loading="loading">
    <div class="cart-header">
      <h2>我的购物车</h2>
      <el-button 
        type="primary" 
        :disabled="!cartItems.length"
        @click="handleCheckout"
      >
        去结算
      </el-button>
    </div>

    <!-- 购物车为空显示 -->
    <div v-if="!cartItems.length" class="empty-cart">
      <el-empty description="购物车是空的">
        <el-button type="primary" @click="$router.push('/products')">
          去购物
        </el-button>
      </el-empty>
    </div>

    <template v-else>
      <!-- 购物车列表 -->
      <div class="cart-list">
        <el-card v-for="item in cartItems" :key="item.id" class="cart-item">
          <div class="item-content">
            <!-- 选择框 -->
            <div class="item-checkbox">
              <el-checkbox 
                v-model="item.selected"
                @change="(val) => handleSelectedChange(item, val)"
              />
            </div>

            <!-- 商品图片 -->
            <div class="item-image">
              <img :src="item.product.imageUrl || defaultImage" :alt="item.product.name" @error="handleImageError">
            </div>

            <!-- 商品信息 -->
            <div class="item-info">
              <h3 class="item-name" @click="$router.push(`/product/${item.product.id}`)">
                {{ item.product.name }}
              </h3>
              <p class="item-price">¥{{ item.product.price }}</p>
            </div>

            <!-- 数量控制 -->
            <div class="item-quantity">
              <el-input-number 
                v-model="item.quantity" 
                :min="1" 
                :max="item.product.stock || 99"
                size="small"
                @change="(val) => handleQuantityChange(item, val)"
              />
            </div>

            <!-- 小计 -->
            <div class="item-subtotal">
              <p class="subtotal-label">小计</p>
              <p class="subtotal-amount">¥{{ (item.product.price * item.quantity).toFixed(2) }}</p>
            </div>

            <!-- 操作按钮 -->
            <div class="item-actions">
              <el-button 
                type="danger" 
                link
                @click="handleRemoveItem(item)"
              >
                删除
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 购物车底部 -->
      <div class="cart-footer">
        <div class="cart-summary">
          <div class="summary-item">
            <span>商品总数：</span>
            <span>{{ totalCount }} 件</span>
          </div>
          <div class="summary-item">
            <span>商品总价：</span>
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
        </div>
        <el-button 
          type="primary" 
          size="large"
          @click="handleCheckout"
        >
          去结算 ({{ totalCount }}件)
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getCartItems, updateCartItem, removeFromCart, updateCartItemSelected } from '@/api/cart'

const router = useRouter()
const userStore = useUserStore()
const cartItems = ref([])
const loading = ref(false)
const defaultImage = 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'

// 计算总数量
const totalCount = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.quantity, 0)
})

// 计算总价格
const totalPrice = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.product.price * item.quantity, 0)
})

// 获取购物车列表
const fetchCartItems = async () => {
  try {
    loading.value = true
    const response = await getCartItems()
    console.log('购物车响应数据:', response)
    
    if (response.code === 200) {
      // API直接返回数组，不需要访问records
      if (response.data && Array.isArray(response.data)) {
        cartItems.value = response.data.map(item => ({
          ...item,
          selected: item.selected ?? true // 如果selected未定义则默认选中
        }))
      } else {
        cartItems.value = []
      }
      console.log('处理后的购物车数据:', cartItems.value)
    } else {
      console.error('获取购物车失败:', response)
      cartItems.value = []
    }
  } catch (error) {
    console.error('获取购物车出错:', error)
    cartItems.value = []
  } finally {
    loading.value = false
  }
}

// 处理数量变化
const handleQuantityChange = async (item, quantity) => {
  try {
    const response = await updateCartItem(item.id, quantity)
    if (response.code === 200) {
      ElMessage.success('数量已更新')
      await fetchCartItems() // 重新获取购物车列表
    }
  } catch (error) {
    console.error('Failed to update quantity:', error)
    ElMessage.error('更新数量失败')
  }
}

// 处理移除商品
const handleRemoveItem = (item) => {
  ElMessageBox.confirm(
    '确定要从购物车中删除该商品吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await removeFromCart(item.id)
      if (response.code === 200) {
        ElMessage.success('商品已删除')
        await fetchCartItems() // 重新获取购物车列表
      }
    } catch (error) {
      console.error('Failed to remove item:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 处理结算
const handleCheckout = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({
      path: '/login',
      query: { redirect: '/cart' }
    })
    return
  }
  
  const selectedItems = cartItems.value.filter(item => item.selected)
  if (selectedItems.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }

  try {
    // 构建结算商品数据
    const items = selectedItems.map(item => ({
      productId: item.product.id,
      quantity: item.quantity,
      price: item.product.price,
      cartItemId: item.id
    }))

    // 跳转到结算页面，携带选中的商品信息
    await router.push({
      path: '/order/settlement',
      query: {
        items: JSON.stringify(items)
      }
    })

    // 删除已结算的商品
    const promises = selectedItems.map(item => removeFromCart(item.id))
    await Promise.all(promises)
    
    // 重新获取购物车列表
    await fetchCartItems()
  } catch (error) {
    console.error('结算失败:', error)
    ElMessage.error('结算失败，请重试')
  }
}

// 处理图片加载失败
const handleImageError = (e) => {
  e.target.src = defaultImage
}

// 处理选中状态变化
const handleSelectedChange = async (item, selected) => {
  try {
    const response = await updateCartItemSelected(item.id, selected)
    if (response.code === 200) {
      await fetchCartItems() // 重新获取购物车列表
    }
  } catch (error) {
    console.error('Failed to update selected status:', error)
    ElMessage.error('更新状态失败')
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCartItems()
})
</script>

<style scoped>
.cart-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.cart-header h2 {
  margin: 0;
  color: #333;
}

.empty-cart {
  background: white;
  padding: 40px;
  border-radius: 8px;
  text-align: center;
}

.cart-list {
  margin-bottom: 20px;
}

.cart-item {
  margin-bottom: 15px;
}

.item-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.item-image {
  width: 100px;
  height: 100px;
  margin-right: 20px;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.item-info {
  flex: 1;
}

.item-name {
  margin: 0 0 10px;
  font-size: 16px;
  color: #333;
  cursor: pointer;
}

.item-name:hover {
  color: #409EFF;
}

.item-price {
  margin: 0;
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.item-quantity {
  margin: 0 40px;
}

.item-subtotal {
  margin: 0 40px;
  text-align: center;
}

.subtotal-label {
  margin: 0 0 5px;
  color: #666;
  font-size: 14px;
}

.subtotal-amount {
  margin: 0;
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.cart-footer {
  background: white;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-summary {
  display: flex;
  gap: 40px;
}

.summary-item {
  font-size: 16px;
}

.total-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.el-button--large {
  padding: 12px 40px;
  font-size: 16px;
}

.item-checkbox {
  margin-right: 20px;
  display: flex;
  align-items: center;
}

.item-actions {
  margin-left: 20px;
  padding-left: 20px;
  border-left: 1px solid #eee;
}
</style> 
<template>
  <div class="order-list-page">
    <h2>我的订单</h2>
    
    <div v-loading="loading">
      <el-empty v-if="visibleOrders.length === 0" description="暂无订单" />
      <order-list 
        v-else 
        :orders="visibleOrders" 
        @refresh="fetchOrders"
        @delete="handleDelete"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrders } from '@/api/order'
import OrderList from '@/components/OrderList.vue'

const orders = ref([])
const loading = ref(false)

// 获取隐藏的订单ID列表
const getHiddenOrders = () => {
  const hiddenOrders = localStorage.getItem('hiddenOrders')
  return hiddenOrders ? JSON.parse(hiddenOrders) : []
}

// 添加订单到隐藏列表
const hideOrder = (orderId) => {
  const hiddenOrders = getHiddenOrders()
  if (!hiddenOrders.includes(orderId)) {
    hiddenOrders.push(orderId)
    localStorage.setItem('hiddenOrders', JSON.stringify(hiddenOrders))
  }
}

// 过滤掉隐藏的订单
const visibleOrders = computed(() => {
  const hiddenOrders = getHiddenOrders()
  return orders.value.filter(order => !hiddenOrders.includes(order.id))
})

// 获取订单列表
const fetchOrders = async () => {
  try {
    loading.value = true
    const response = await getOrders()
    if (response.code === 200 && response.data && response.data.content) {
      orders.value = response.data.content
    } else {
      orders.value = []
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
    orders.value = []
  } finally {
    loading.value = false
  }
}

// 删除订单（实际是隐藏）
const handleDelete = async (order) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？删除后将不再显示', '删除订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    hideOrder(order.id)
    ElMessage.success('订单已删除')
    // 刷新订单列表
    await fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除订单失败，请重试')
    }
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-list-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}
</style> 
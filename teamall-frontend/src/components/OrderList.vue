<template>
  <div class="order-list">
    <el-card v-for="order in orders" :key="order.id" class="order-item">
      <!-- 订单头部信息 -->
      <div class="order-header">
        <div class="order-info">
          <span class="order-id">订单号：{{ order.id }}</span>
          <span class="order-time">下单时间：{{ formatDate(order.createTime) }}</span>
        </div>
        <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
      </div>

      <!-- 订单商品列表 -->
      <div class="order-content">
        <div v-for="item in order.items" :key="item.id" class="product-item" @click="goToOrderDetail(order.id)">
          <el-image 
            :src="item.product?.imageUrl || defaultImage" 
            :alt="item.product?.name"
            class="product-image"
            @error="handleImageError"
          />
          <div class="product-info">
            <h4>{{ item.product?.name }}</h4>
            <div class="price-quantity">
              <span class="price">￥{{ item.price }}</span>
              <span class="quantity">x {{ item.quantity }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单底部信息 -->
      <div class="order-footer">
        <div class="total-info">
          <span>共 {{ getTotalQuantity(order) }} 件商品</span>
          <span class="total-amount">总计：￥{{ order.totalAmount }}</span>
        </div>
        <div class="actions">
          <el-button 
            v-if="order.status === 'PENDING'" 
            type="primary"
            size="small"
            @click="handlePay(order)"
          >
            立即支付
          </el-button>
          <el-button 
            type="primary" 
            plain
            size="small"
            @click="goToOrderDetail(order.id)"
          >
            查看详情
          </el-button>
          <el-button 
            v-if="order.status === 'COMPLETED'" 
            type="danger" 
            plain
            size="small"
            @click="$emit('delete', order)"
          >
            删除订单
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-item {
  margin-bottom: 0;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.order-info {
  display: flex;
  gap: 20px;
  color: #666;
}

.order-id {
  font-weight: bold;
}

.order-content {
  padding: 15px 0;
  cursor: pointer;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  margin-right: 15px;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #333;
}

.price-quantity {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #666;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.total-info {
  display: flex;
  gap: 20px;
  color: #666;
}

.total-amount {
  color: #f56c6c;
  font-weight: bold;
}

.actions {
  display: flex;
  gap: 10px;
}
</style>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { payOrder } from '@/api/order'

const props = defineProps({
  orders: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['refresh', 'delete'])
const router = useRouter()
const defaultImage = 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

// 获取状态样式
const getStatusType = (status) => {
  const types = {
    PENDING: 'warning',
    PAID: 'success',
    PENDING_DELIVERY: 'info',
    SHIPPED: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    PENDING: '待支付',
    PAID: '已支付',
    PENDING_DELIVERY: '待发货',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return texts[status] || status
}

// 计算订单总数量
const getTotalQuantity = (order) => {
  return order.items.reduce((sum, item) => sum + item.quantity, 0)
}

// 跳转到订单详情
const goToOrderDetail = (orderId) => {
  router.push(`/order/detail/${orderId}`)
}

// 处理支付
const handlePay = async (order) => {
  try {
    const response = await payOrder(order.id, 'ALIPAY')
    if (response.code === 200) {
      ElMessage.success('支付成功')
      emit('refresh')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败，请重试')
  }
}

// 处理图片加载失败
const handleImageError = (e) => {
  e.target.src = defaultImage
}
</script> 
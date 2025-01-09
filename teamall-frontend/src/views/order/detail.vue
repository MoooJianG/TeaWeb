<template>
  <div class="order-detail">
    <h2>订单详情</h2>
    
    <div v-loading="loading" v-if="order">
      <!-- 订单状态 -->
      <div class="section status-section">
        <div class="status">
          <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
        </div>
      </div>

      <!-- 收货信息 -->
      <div class="section">
        <h3>收货信息</h3>
        <div class="address-info">
          <p><span class="label">收货人：</span>{{ order.receiverName }}</p>
          <p><span class="label">联系电话：</span>{{ order.receiverPhone }}</p>
          <p><span class="label">收货地址：</span>{{ formatAddress(order) }}</p>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="section">
        <h3>商品信息</h3>
        <div v-for="item in order.items" :key="item.id" class="product-info">
          <el-image 
            :src="item.product?.imageUrl || defaultImage" 
            class="product-image"
            @error="handleImageError"
          />
          <div class="product-details">
            <h4>{{ item.product?.name }}</h4>
            <div class="price-quantity">
              <span class="price">￥{{ item.price }}</span>
              <span class="quantity">x {{ item.quantity }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="section">
        <h3>订单信息</h3>
        <div class="order-info">
          <p><span class="label">订单编号：</span>{{ order.id }}</p>
          <p><span class="label">创建时间：</span>{{ formatDate(order.createTime) }}</p>
          <p><span class="label">订单金额：</span><span class="price">￥{{ order.totalAmount }}</span></p>
        </div>
      </div>

      <!-- 订单操作 -->
      <div class="section actions-section">
        <div class="action-buttons">
          <!-- 待支付状态的操作 -->
          <el-button 
            v-if="order.status === 'PENDING'" 
            type="primary"
            :loading="loading"
            @click="handlePay"
          >
            立即支付
          </el-button>
          
          <!-- 已发货状态的操作 -->
          <el-button 
            v-if="order.status === 'SHIPPED'" 
            type="success"
            :loading="loading"
            @click="handleConfirm"
          >
            确认收货
          </el-button>
          
          <!-- 可取消的状态（待支付、已支付）都显示取消按钮 -->
          <el-button 
            v-if="['PENDING', 'PAID'].includes(order.status)" 
            type="danger"
            plain
            :loading="loading"
            @click="handleCancel"
          >
            取消订单
          </el-button>
          
          <!-- 评价商品按钮 -->
          <el-button 
            v-if="order.status === 'COMPLETED' && !order.reviewed" 
            type="primary"
            plain
            @click="handleReview"
          >
            评价商品
          </el-button>
        </div>
      </div>
    </div>
  </div>

  <!-- 评价商品对话框 -->
  <el-dialog
    v-model="reviewDialogVisible"
    title="评价商品"
    width="50%"
  >
    <div class="review-products">
      <div v-for="item in order.items" :key="item.id" class="review-item">
        <div class="product-info">
          <img :src="item.product.imageUrl" :alt="item.product.name" class="product-image">
          <div class="product-details">
            <h4>{{ item.product.name }}</h4>
            <p class="price">¥{{ item.price }}</p>
          </div>
        </div>
        
        <div class="review-content">
          <div class="rating">
            <span class="label">评分：</span>
            <el-rate v-model="reviewForms[item.id].rating" />
          </div>
          <div class="comment">
            <span class="label">评价内容：</span>
            <el-input
              v-model="reviewForms[item.id].content"
              type="textarea"
              rows="3"
              placeholder="请输入您的评价内容"
            />
          </div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReviews">提交评价</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrder, payOrder, cancelOrder, completeOrder, updateOrderStatus } from '@/api/order'
import { createReview } from '@/api/review'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(false)
const defaultImage = 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'
const reviewDialogVisible = ref(false)
const reviewForms = ref({})

// 获取订单详情
const fetchOrder = async () => {
  try {
    loading.value = true
    const response = await getOrder(route.params.id)
    if (response.code === 200) {
      order.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

// 格式化地址
const formatAddress = (order) => {
  if (!order) return ''
  return `${order.receiverProvince}${order.receiverCity}${order.receiverDistrict}${order.receiverAddress}`
}

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
    PENDING_RECEIPT: 'primary',
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

// 支付订单
const handlePay = async () => {
  try {
    loading.value = true
    const response = await payOrder(order.value.id, 'ALIPAY')
    if (response.code === 200) {
      ElMessage.success('支付成功')
      await fetchOrder() // 刷新订单详情
      
      // 支付成功后自动发货
      try {
        const updateResponse = await updateOrderStatus(order.value.id, 'SHIPPED')
        if (updateResponse.code === 200) {
          await fetchOrder() // 刷新订单详情
          ElMessage.info('订单已发货')
        }
      } catch (error) {
        console.error('自动发货失败:', error)
        ElMessage.error('自动发货失败，请联系客服')
      }
    } else {
      throw new Error(response.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.message || '支付失败，请重试')
  } finally {
    loading.value = false
  }
}

// 取消订单
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const response = await cancelOrder(order.value.id)
    if (response.code === 200) {
      ElMessage.success('订单已取消')
      await fetchOrder()
    } else {
      throw new Error(response.message || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error(error.message || '取消订单失败')
    }
  } finally {
    loading.value = false
  }
}

// 确认收货
const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const response = await updateOrderStatus(order.value.id, 'COMPLETED')
    if (response.code === 200) {
      ElMessage.success('确认收货成功')
      await fetchOrder()
    } else {
      throw new Error(response.message || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error(error.message || '确认收货失败')
    }
  } finally {
    loading.value = false
  }
}

// 处理图片加载失败
const handleImageError = (e) => {
  const img = e.target
  if (img) {
    img.src = defaultImage
  }
}

// 处理评价
const handleReview = () => {
  // 初始化每个商品的评价表单
  order.value.items.forEach(item => {
    reviewForms.value[item.id] = {
      rating: 5,
      content: ''
    }
  })
  reviewDialogVisible.value = true
}

// 提交评价
const submitReviews = async () => {
  // 验证表单
  const hasEmptyReview = Object.values(reviewForms.value).some(form => {
    return !form.content.trim()
  })
  
  if (hasEmptyReview) {
    ElMessage.warning('请填写所有商品的评价内容')
    return
  }
  
  try {
    loading.value = true
    
    // 获取所有评价数据
    const reviews = Object.entries(reviewForms.value).map(([itemId, form]) => {
      const orderItem = order.value.items.find(item => item.id === parseInt(itemId))
      return {
        orderId: order.value.id,
        productId: orderItem.product.id,  // 使用商品ID而不是订单项ID
        rating: form.rating,
        content: form.content.trim()
      }
    })
    
    // 提交所有评价
    const promises = reviews.map(review => createReview(review))
    const results = await Promise.all(promises)
    
    // 检查是否所有评价都提交成功
    const allSuccess = results.every(res => res.code === 200)
    
    if (allSuccess) {
      ElMessage.success('评价提交成功')
      reviewDialogVisible.value = false
      // 刷新订单详情
      await fetchOrder()
    } else {
      ElMessage.error('部分评价提交失败，请重试')
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error('评价提交失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrder()
})
</script>

<style scoped>
.order-detail {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}

.order-detail h2 {
  margin: 0 0 20px;
  color: #333;
}

.section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.section h3 {
  margin: 0 0 15px;
  font-size: 16px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.status-section {
  text-align: center;
  padding: 30px;
}

.status .el-tag {
  font-size: 16px;
  padding: 8px 20px;
}

.label {
  color: #666;
  display: inline-block;
  width: 80px;
}

.address-info p,
.order-info p {
  margin: 10px 0;
  line-height: 1.6;
}

.product-info {
  display: flex;
  gap: 20px;
  padding: 15px;
  background: #f8f8f8;
  border-radius: 4px;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
}

.product-details {
  flex: 1;
}

.product-details h4 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #333;
}

.price-quantity {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: bold;
}

.quantity {
  color: #666;
}

.actions-section {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.el-button {
  min-width: 120px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  min-width: 120px;
}

.review-products {
  max-height: 60vh;
  overflow-y: auto;
}

.review-item {
  border-bottom: 1px solid #eee;
  padding: 20px 0;
}

.review-item:last-child {
  border-bottom: none;
}

.product-info {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.product-details h4 {
  margin: 0 0 8px;
  font-size: 16px;
}

.price {
  color: #f56c6c;
  margin: 0;
}

.review-content {
  padding-left: 95px;
}

.rating, .comment {
  margin-bottom: 15px;
}

.label {
  display: inline-block;
  width: 80px;
  color: #606266;
}
</style> 
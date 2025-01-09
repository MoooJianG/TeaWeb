<template>
  <div class="review-create">
    <h2>商品评价</h2>
    
    <div class="review-form" v-loading="loading">
      <!-- 商品信息 -->
      <div class="product-info">
        <el-image 
          :src="order?.items[0]?.product?.imageUrl || defaultImage" 
          class="product-image"
          @error="handleImageError"
        />
        <div class="product-details">
          <h3>{{ order?.items[0]?.product?.name }}</h3>
          <div class="price">￥{{ order?.items[0]?.price }}</div>
        </div>
      </div>

      <!-- 评分 -->
      <div class="form-item">
        <div class="label">商品评分</div>
        <el-rate
          v-model="reviewForm.rating"
          :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          show-score
        />
      </div>

      <!-- 评价内容 -->
      <div class="form-item">
        <div class="label">评价内容</div>
        <el-input
          v-model="reviewForm.content"
          type="textarea"
          :rows="4"
          placeholder="请分享您对商品的使用感受"
          maxlength="500"
          show-word-limit
        />
      </div>

      <!-- 上传图片 -->
      <div class="form-item">
        <div class="label">上传图片（选填）</div>
        <el-upload
          action="/api/upload"
          list-type="picture-card"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          :on-remove="handleRemove"
          multiple
          :limit="5"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </div>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button @click="router.back()">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting"
          @click="handleSubmit"
        >
          提交评价
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getOrder } from '@/api/order'
import { createReview } from '@/api/review'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(false)
const submitting = ref(false)
const defaultImage = 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'

const reviewForm = ref({
  rating: 5,
  content: '',
  images: []
})

// 获取订单信息
const fetchOrder = async () => {
  try {
    loading.value = true
    const response = await getOrder(route.params.id)
    if (response.code === 200) {
      order.value = response.data
      // 检查订单状态
      if (order.value.status !== 'COMPLETED') {
        ElMessage.warning('只能评价已完成的订单')
        router.back()
      }
    }
  } catch (error) {
    console.error('Failed to load order:', error)
    ElMessage.error('获取订单信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 处理图片上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    reviewForm.value.images.push(response.data)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

// 处理图片上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败')
}

// 上传前检查
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return true
}

// 移除图片
const handleRemove = (file) => {
  const index = reviewForm.value.images.indexOf(file.url)
  if (index !== -1) {
    reviewForm.value.images.splice(index, 1)
  }
}

// 处理图片加载失败
const handleImageError = (e) => {
  const img = e.target
  if (img) {
    img.src = defaultImage
  }
}

// 提交评价
const handleSubmit = async () => {
  if (!reviewForm.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }

  try {
    submitting.value = true
    const response = await createReview({
      orderId: order.value.id,
      productId: order.value.items[0].product.id,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content.trim(),
      images: reviewForm.value.images
    })

    if (response.code === 200) {
      ElMessage.success('评价成功')
      router.push(`/order/detail/${order.value.id}`)
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '评价失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchOrder()
})
</script>

<style scoped>
.review-create {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.review-create h2 {
  margin: 0 0 20px;
  color: #333;
}

.review-form {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.product-info {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #f8f9fb;
  border-radius: 8px;
  margin-bottom: 30px;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
}

.product-details {
  flex: 1;
}

.product-details h3 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #333;
}

.price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: bold;
}

.form-item {
  margin-bottom: 25px;
}

.label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style> 
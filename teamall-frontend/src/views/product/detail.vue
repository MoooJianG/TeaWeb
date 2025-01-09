<template>
  <div class="product-detail">
    <div class="product-info">
      <div class="product-image">
        <el-image 
          :src="product.imageUrl || defaultImage" 
          fit="cover"
          @error="handleImageError"
        />
      </div>
      <div class="product-content">
        <h1>{{ product.name }}</h1>
        <div class="price">￥{{ product.price }}</div>
        <div class="stock-info">
          <span class="label">库存：</span>
          <span class="value">{{ product.stock }}</span>
        </div>
        <div class="category-info">
          <span class="label">分类：</span>
          <span class="value">{{ product.category?.name }}</span>
        </div>
        <div class="description">
          <div class="label">商品描述：</div>
          <p class="value">{{ product.description }}</p>
        </div>
        <div class="purchase-area">
          <div class="quantity">
            <span class="label">数量：</span>
            <el-input-number 
              v-model="quantity" 
              :min="1" 
              :max="product.stock"
              @change="handleQuantityChange"
            />
          </div>
          <div class="actions">
            <el-button type="primary" size="large" @click="handleBuyNow">立即购买</el-button>
            <el-button type="success" size="large" @click="handleAddToCart">加入购物车</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 评论区域 -->
    <div class="reviews-section">
      <h2>商品评价</h2>
      <div v-if="reviews.length === 0" class="no-reviews">
        <el-empty description="暂无评论" />
      </div>
      <div v-else class="reviews-list">
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-header">
            <div class="user-info">
              <el-avatar :src="review.user.avatarUrl" :size="40">
                {{ review.user.username?.charAt(0) }}
              </el-avatar>
              <div class="user-meta">
                <span class="username">{{ review.user.username }}</span>
                <span class="review-time">{{ formatDate(review.createdAt) }}</span>
              </div>
            </div>
            <div class="rating">
              <el-rate
                v-model="review.rating"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <div class="review-content">
            <p>{{ review.content }}</p>
            <div v-if="review.images && review.images.length" class="review-images">
              <el-image
                v-for="(image, index) in review.images"
                :key="index"
                :src="image"
                :preview-src-list="review.images"
                fit="cover"
                class="review-image"
              />
            </div>
          </div>
          <div v-if="review.reply" class="review-reply">
            <div class="reply-header">
              <el-tag size="small" type="primary">商家回复</el-tag>
              <span class="reply-time">{{ formatDate(review.updatedAt) }}</span>
            </div>
            <p>{{ review.reply }}</p>
          </div>
          <!-- 管理员回复按钮 -->
          <div v-if="userStore.isAdmin && !review.reply" class="review-actions">
            <el-button 
              type="primary" 
              link
              @click="handleReply(review)"
            >
              回复
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 加载更多评论 -->
      <div v-if="hasMoreReviews && reviews.length > 0" class="load-more">
        <el-button :loading="loadingReviews" @click="loadMoreReviews">
          加载更多评论
        </el-button>
      </div>
    </div>
    
    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复评论"
      width="500px"
    >
      <el-form :model="replyForm">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            :loading="submitting"
            @click="submitReply"
          >
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProduct } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useUserStore } from '@/store/user'
import { getProductReviews, replyReview } from '@/api/review'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref({})
const quantity = ref(1)
const defaultImage = 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'

// 评论相关
const reviews = ref([])
const reviewPage = ref(0)
const reviewPageSize = ref(10)
const hasMoreReviews = ref(true)
const loadingReviews = ref(false)

// 回复相关
const replyDialogVisible = ref(false)
const submitting = ref(false)
const replyForm = ref({
  reviewId: null,
  content: ''
})

// 获取商品评论
const fetchReviews = async (page = 0) => {
  try {
    loadingReviews.value = true
    const response = await getProductReviews(product.value.id, {
      page,
      size: reviewPageSize.value
    })
    
    if (response.code === 200) {
      const { content, last } = response.data
      if (page === 0) {
        reviews.value = content
      } else {
        reviews.value.push(...content)
      }
      hasMoreReviews.value = !last
    }
  } catch (error) {
    console.error('Failed to load reviews:', error)
    ElMessage.error('加载评论失败')
  } finally {
    loadingReviews.value = false
  }
}

// 加载更多评论
const loadMoreReviews = () => {
  if (!loadingReviews.value && hasMoreReviews.value) {
    reviewPage.value++
    fetchReviews(reviewPage.value)
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取商品详情
const fetchProduct = async () => {
  try {
    const response = await getProduct(route.params.id)
    product.value = response.data
    // 在获取到商品信息后再获取评论
    fetchReviews()
  } catch (error) {
    ElMessage.error('获取商品信息失败')
  }
}

// 处理数量变化
const handleQuantityChange = (value) => {
  if (value > product.value.stock) {
    ElMessage.warning('超出库存数量')
    quantity.value = product.value.stock
  }
}

// 加入购物车
const handleAddToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  try {
    const response = await addToCart({
      productId: product.value.id,
      quantity: quantity.value
    })
    
    if (response.code === 200) {
      ElMessage.success('已添加到购物车')
    }
  } catch (error) {
    console.error('Failed to add to cart:', error)
    ElMessage.error('添加购物车失败，请稍后重试')
  }
}

// 立即购买
const handleBuyNow = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  // 构建商品信息
  const items = [{
    productId: product.value.id,
    name: product.value.name,
    price: product.value.price,
    quantity: quantity.value,
    imageUrl: product.value.imageUrl
  }]

  // 跳转到订单结算页面，携带商品信息
  router.push({
    path: '/order/settlement',
    query: {
      items: JSON.stringify(items)
    }
  })
}

// 处理图片加载失败
const handleImageError = () => {
  product.value.imageUrl = defaultImage
}

// 处理回复
const handleReply = (review) => {
  replyForm.value.reviewId = review.id
  replyForm.value.content = ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyForm.value.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  try {
    submitting.value = true
    const response = await replyReview(replyForm.value.reviewId, replyForm.value.content.trim())

    if (response.code === 200) {
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      // 刷新评论列表
      await fetchReviews(reviewPage.value)
    } else {
      throw new Error(response.message || '回复失败')
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error(error.message || '回复失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchProduct()
})
</script>

<style scoped>
.product-detail {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.product-info {
  display: flex;
  gap: 40px;
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.product-image {
  width: 400px;
  height: 400px;
  flex-shrink: 0;
}

.product-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.product-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-content h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
  font-weight: bold;
}

.price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
}

.label {
  color: #666;
  font-size: 14px;
  min-width: 70px;
  display: inline-block;
}

.value {
  color: #333;
  font-size: 14px;
}

.stock-info, .category-info {
  display: flex;
  align-items: center;
}

.description {
  margin-top: 10px;
}

.description .label {
  margin-bottom: 8px;
}

.description .value {
  line-height: 1.6;
  color: #666;
  margin: 0;
}

.purchase-area {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.quantity {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 20px;
}

.el-button {
  width: 180px;
  height: 45px;
  font-size: 16px;
}

.reviews-section {
  margin-top: 30px;
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.reviews-section h2 {
  margin: 0 0 20px;
  font-size: 20px;
  color: #333;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  color: #333;
  font-weight: 500;
}

.review-content {
  margin-bottom: 15px;
}

.review-content p {
  margin: 0;
  line-height: 1.6;
  color: #666;
}

.review-images {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.review-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  cursor: pointer;
}

.review-reply {
  margin-top: 15px;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.reply-header {
  color: #409eff;
  font-weight: 500;
  margin-bottom: 8px;
}

.review-reply p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}

.review-footer {
  margin-top: 10px;
}

.review-time {
  color: #999;
  font-size: 12px;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}

.no-reviews {
  padding: 40px 0;
  text-align: center;
  color: #909399;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  color: #333;
  font-weight: 500;
  line-height: 1.2;
}

.review-time {
  color: #999;
  font-size: 12px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.reply-time {
  color: #999;
  font-size: 12px;
}

.review-reply {
  margin-top: 15px;
  background-color: #f8f9fb;
  padding: 15px;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.review-reply p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}
</style> 

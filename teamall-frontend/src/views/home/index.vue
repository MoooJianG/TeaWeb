<template>
  <div class="home">
    <!-- 轮播图部分 -->
    <el-carousel height="500px" class="banner">
      <el-carousel-item v-for="item in banners" :key="item.id">
        <div class="banner-item" :style="{ backgroundImage: `url(${item.image})` }">
          <div class="banner-content">
            <h2>{{ item.title }}</h2>
            <p>{{ item.description }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 茶文化专区 -->
    <section class="tea-culture">
      <h2 class="section-title">茶文化精选</h2>
      <div class="culture-grid">
        <div v-for="item in teaCultures" :key="item.id" class="culture-item">
          <el-card :body-style="{ padding: '0px' }" shadow="hover">
            <img :src="item.image" class="culture-image">
            <div class="culture-info">
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </div>
          </el-card>
        </div>
      </div>
    </section>

    <!-- 推荐商品 -->
    <section class="featured-products">
      <h2 class="section-title">精选好茶</h2>
      <el-row :gutter="20">
        <el-col v-for="product in products" 
                :key="product.id" 
                :xs="24" 
                :sm="12" 
                :md="8" 
                :lg="6">
          <el-card class="product-card" shadow="hover" @click="goToProduct(product.id)">
            <el-image 
              :src="product.imageUrl" 
              class="product-image"
              @error="handleImageError"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
              <template #placeholder>
                <div class="image-slot">
                  <el-icon><Loading /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="description">{{ product.description }}</p>
              <p class="price">¥{{ product.price.toFixed(2) }}</p>
              <div class="product-actions">
                <el-button type="primary" size="small" @click.stop="goToProduct(product.id)">
                  查看详情
                </el-button>
                <el-button type="success" size="small" @click.stop="addToCartHandler(product)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 加载更多 -->
      <div class="load-more" v-if="hasMore">
        <el-button :loading="loading" @click="loadMore">加载更多</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getProducts } from '@/api/product'
import { addToCart } from '@/api/cart'
import { Picture, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 轮播图数据
const banners = ref([
  {
    id: 1,
    title: '传统茶艺文化',
    description: '感受东方茶道之美',
    image: 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'
  },
  {
    id: 2,
    title: '精选名茶',
    description: '严选优质茶叶，品味自然馈赠',
    image: 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'
  }
])

// 茶文化数据
const teaCultures = ref([
  {
    id: 1,
    title: '茶道礼仪',
    description: '探索传统茶道礼仪的精髓',
    image: 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'
  },
  {
    id: 2,
    title: '茶叶知识',
    description: '了解不同茶叶的特点与功效',
    image: 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'
  },
  {
    id: 3,
    title: '品茶艺术',
    description: '品味茶香，感受茶道之美',
    image: 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'
  }
])

// 产品列表相关
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const loading = ref(false)
const hasMore = ref(true)

// 在 script setup 部分添加默认图片常量
const DEFAULT_IMAGE = 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'

// 获取产品列表
const loadProducts = async (page = 1) => {
  try {
    loading.value = true
    const response = await getProducts({
      page: page - 1, // 后端分页从0开始
      size: pageSize.value
    })
    
    if (response.code === 200) {
      const { content, totalElements } = response.data
      if (page === 1) {
        products.value = content
      } else {
        products.value.push(...content)
      }
      total.value = totalElements
      hasMore.value = products.value.length < total.value
    }
  } catch (error) {
    console.error('Failed to load products:', error)
    ElMessage.error('加载商品失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    currentPage.value++
    loadProducts(currentPage.value)
  }
}

// 跳转到商品详情
const goToProduct = (id) => {
  router.push(`/product/${id}`)
}

// 添加到购物车
const addToCartHandler = async (product) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({
      path: '/login',
      query: { redirect: router.currentRoute.value.fullPath }
    })
    return
  }
  
  try {
    const response = await addToCart({
      productId: product.id,
      quantity: 1
    })
    
    if (response.code === 200) {
      ElMessage.success('已添加到购物车')
    }
  } catch (error) {
    console.error('Failed to add to cart:', error)
    ElMessage.error('添加购物车失败，请稍后重试')
  }
}

// 处理图片加载失败
const handleImageError = (e) => {
  const img = e.target
  if (img) {
    img.src = DEFAULT_IMAGE
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.home {
  margin-top: -20px;
}

.banner {
  margin-bottom: 40px;
}

.banner-item {
  height: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-content {
  text-align: center;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  padding: 20px 40px;
  border-radius: 8px;
  max-width: 80%;
}

.banner-content h2 {
  font-size: 2.5em;
  margin-bottom: 10px;
}

.section-title {
  text-align: center;
  color: #18624b;
  margin: 40px 0;
  font-size: 2em;
  font-weight: bold;
}

.culture-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
  padding: 0 20px;
}

.culture-item {
  transition: transform 0.3s;
}

.culture-item:hover {
  transform: translateY(-5px);
}

.culture-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.culture-info {
  padding: 15px;
}

.culture-info h3 {
  color: #18624b;
  margin-bottom: 10px;
}

.featured-products {
  padding: 0 20px;
  margin-bottom: 40px;
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 15px;
  text-align: center;
}

.product-info h3 {
  color: #333;
  margin-bottom: 10px;
  font-size: 1.2em;
}

.description {
  color: #666;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price {
  color: #e65d17;
  font-size: 1.2em;
  font-weight: bold;
  margin: 10px 0;
}

.product-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .banner-content {
    padding: 15px;
  }
  
  .banner-content h2 {
    font-size: 1.8em;
  }
  
  .section-title {
    font-size: 1.5em;
  }
  
  .culture-grid {
    grid-template-columns: 1fr;
  }
}

.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}

.image-slot .el-icon {
  font-size: 32px;
  margin-bottom: 10px;
}
</style> 
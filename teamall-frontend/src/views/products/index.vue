<template>
  <div class="products">
    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索商品..."
        class="search-input"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select
        v-model="selectedCategory"
        placeholder="选择分类"
        clearable
        @change="handleCategoryChange"
      >
        <el-option
          v-for="category in categories"
          :key="category.id"
          :label="category.name"
          :value="category.id"
        >
          <div class="category-option">
            <span class="category-name">{{ category.name }}</span>
            <span class="category-description">{{ category.description }}</span>
          </div>
        </el-option>
      </el-select>

      <el-button 
        type="primary" 
        plain 
        @click="clearFilters"
        :disabled="!hasActiveFilters"
      >
        清除筛选
      </el-button>
    </div>

    <!-- 商品列表 -->
    <el-row :gutter="20" v-loading="loading">
      <el-col 
        v-for="product in products" 
        :key="product.id" 
        :xs="24" 
        :sm="12" 
        :md="8" 
        :lg="6"
      >
        <el-card class="product-card" shadow="hover" @click="goToProduct(product.id)">
          <el-image 
            :src="product.imageUrl" 
            class="product-image"
            @error="(e) => product.imageUrl = DEFAULT_IMAGE"
          >
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
                <span>加载中...</span>
              </div>
            </template>
            <template #placeholder>
              <div class="image-slot">
                <el-icon><Loading /></el-icon>
                <span>加载中...</span>
              </div>
            </template>
          </el-image>
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p class="description">{{ product.description }}</p>
            <p class="price">¥{{ (product.price || 0).toFixed(2) }}</p>
            <div class="product-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click.stop="goToProduct(product.id)"
              >
                查看详情
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                @click.stop="handleAddToCart(product)"
              >
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

    <!-- 无数据提示 -->
    <el-empty
      v-if="!loading && products.length === 0"
      description="暂无商品"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Picture, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getProducts, searchProducts } from '@/api/product'
import { getCategories } from '@/api/category'
import { addToCart as apiAddToCart } from '@/api/cart'

const router = useRouter()
const userStore = useUserStore()

// 搜索和筛选
const searchQuery = ref('')
const selectedCategory = ref('')
const sortBy = ref('')
const categories = ref([])

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await getCategories()
    if (response.code === 200) {
      categories.value = response.data
    }
  } catch (error) {
    console.error('Failed to load categories:', error)
    ElMessage.error('加载分类失败')
  }
}

// 产品列表
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(false)
const hasMore = ref(true)

// 在 script setup 部分添加默认图片常量
const DEFAULT_IMAGE = 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'

// 获取产品列表
const loadProducts = async (page = 1, isSearch = false) => {
  try {
    loading.value = true
    const params = {
      page: page - 1,
      size: pageSize.value,
      categoryId: selectedCategory.value || null,
      keyword: searchQuery.value || null
    }
    
    console.log('请求参数:', params)
    const response = await getProducts(params)
    handleResponse(response)
    
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 处理API响应
const handleResponse = (response) => {
  console.log('商品列表响应:', response)
  if (response.code === 200) {
    const { content, totalElements } = response.data
    if (currentPage.value === 1) {
      products.value = content
    } else {
      products.value.push(...content)
    }
    total.value = totalElements
    hasMore.value = products.value.length < total.value
  }
}

// 加载更多
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    currentPage.value++
    loadProducts(currentPage.value)
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  products.value = [] // 清空当前商品列表
  loadProducts(1, true)
}

// 处理分类变化
const handleCategoryChange = () => {
  currentPage.value = 1
  products.value = [] // 清空当前商品列表
  searchQuery.value = '' // 清空搜索关键词
  loadProducts(1)
}

// 处理排序变化
const handleSortChange = () => {
  console.log('排序方式改变:', sortBy.value)
  currentPage.value = 1
  products.value = [] // 清空当前商品列表
  loadProducts(1)
}

// 跳转到商品详情
const goToProduct = (id) => {
  router.push(`/product/${id}`)
}

// 添加到购物车
const handleAddToCart = async (product) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({
      path: '/login',
      query: { redirect: router.currentRoute.value.fullPath }
    })
    return
  }
  
  try {
    const response = await apiAddToCart({
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

// 判断是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return searchQuery.value || selectedCategory.value
})

// 清除所有筛选条件
const clearFilters = () => {
  searchQuery.value = ''
  selectedCategory.value = ''
  sortBy.value = ''
  currentPage.value = 1
  loadProducts(1)
}

onMounted(() => {
  fetchCategories()
  loadProducts()
})
</script>

<style scoped>
.products {
  padding: 20px;
}

.filter-section {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  max-width: 300px;
  flex: 1;
  min-width: 200px;
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
  margin-top: 30px;
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    gap: 10px;
  }
  
  .search-input {
    max-width: 100%;
  }
  
  .el-select {
    width: 100%;
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

.category-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-name {
  font-weight: bold;
}

.category-description {
  font-size: 12px;
  color: #666;
  white-space: normal;
  line-height: 1.2;
}
</style> 
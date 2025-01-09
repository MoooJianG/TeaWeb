<template>
  <div class="products">
    <div class="filter-section">
      <el-card>
        <template #header>
          <div class="filter-header">
            <span>商品筛选</span>
            <el-button text @click="resetFilters">重置筛选</el-button>
          </div>
        </template>
        
        <!-- 茶叶分类 -->
        <div class="filter-group">
          <h3>茶叶分类</h3>
          <el-checkbox-group v-model="filters.categories">
            <el-checkbox label="绿茶">绿茶</el-checkbox>
            <el-checkbox label="红茶">红茶</el-checkbox>
            <el-checkbox label="乌龙茶">乌龙茶</el-checkbox>
            <el-checkbox label="普洱茶">普洱茶</el-checkbox>
            <el-checkbox label="花茶">花茶</el-checkbox>
          </el-checkbox-group>
        </div>

        <!-- 价格区间 -->
        <div class="filter-group">
          <h3>价格区间</h3>
          <el-slider
            v-model="filters.priceRange"
            range
            :min="0"
            :max="1000"
            :step="10"
          />
          <div class="price-range-text">
            ¥{{ filters.priceRange[0] }} - ¥{{ filters.priceRange[1] }}
          </div>
        </div>
      </el-card>
    </div>

    <div class="products-section">
      <!-- 排序工具栏 -->
      <div class="toolbar">
        <el-radio-group v-model="sortBy" size="large">
          <el-radio-button label="default">默认排序</el-radio-button>
          <el-radio-button label="price-asc">价格从低到高</el-radio-button>
          <el-radio-button label="price-desc">价格从高到低</el-radio-button>
          <el-radio-button label="sales">销量优先</el-radio-button>
        </el-radio-group>

        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品"
          prefix-icon="Search"
          clearable
          class="search-input"
        />
      </div>

      <!-- 商品列表 -->
      <el-row :gutter="20">
        <el-col 
          v-for="product in filteredProducts" 
          :key="product.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="product-card" @click="goToProduct(product.id)">
            <img :src="product.image" class="product-image">
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="description">{{ product.description }}</p>
              <div class="price-row">
                <span class="price">¥{{ product.price }}</span>
                <span class="sales">月销 {{ product.sales }}</span>
              </div>
              <div class="button-group">
                <el-button type="primary" size="small" @click.stop="addToCart(product)">
                  加入购物车
                </el-button>
                <el-button size="small" @click.stop="goToProduct(product.id)">
                  查看详情
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()

// 筛选条件
const filters = ref({
  categories: [],
  priceRange: [0, 1000]
})

const sortBy = ref('default')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(100)

// 模拟商品数据
const products = ref([
  {
    id: 1,
    name: '明前龙井',
    description: '雨前采摘，清香持久',
    price: 388,
    image: '/images/product1.jpg',
    category: '绿茶',
    sales: 1000
  },
  {
    id: 2,
    name: '武夷大红袍',
    description: '岩韵浓郁，香气四溢',
    price: 468,
    image: '/images/product2.jpg',
    category: '乌龙茶',
    sales: 850
  },
  // 更多商品数据...
])

// 计算筛选后的商品
const filteredProducts = computed(() => {
  return products.value.filter(product => {
    // 分类筛选
    if (filters.value.categories.length && !filters.value.categories.includes(product.category)) {
      return false
    }
    // 价格筛选
    if (product.price < filters.value.priceRange[0] || product.price > filters.value.priceRange[1]) {
      return false
    }
    // 关键词搜索
    if (searchKeyword.value && !product.name.includes(searchKeyword.value)) {
      return false
    }
    return true
  }).sort((a, b) => {
    // 排序
    switch (sortBy.value) {
      case 'price-asc':
        return a.price - b.price
      case 'price-desc':
        return b.price - a.price
      case 'sales':
        return b.sales - a.sales
      default:
        return 0
    }
  })
})

// 重置筛选条件
const resetFilters = () => {
  filters.value.categories = []
  filters.value.priceRange = [0, 1000]
  searchKeyword.value = ''
  sortBy.value = 'default'
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 跳转到商品详情
const goToProduct = (id) => {
  router.push('/product/' + id)
}

// 添加到购物车
const addToCart = (product) => {
  // TODO: 调用购物车 API
  ElMessage.success('已添加到购物车')
}
</script>

<style scoped>
.products {
  display: flex;
  gap: 20px;
}

.filter-section {
  width: 250px;
  flex-shrink: 0;
}

.products-section {
  flex: 1;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-group {
  margin-bottom: 20px;
}

.filter-group h3 {
  margin-bottom: 10px;
  color: #333;
  font-size: 16px;
}

.price-range-text {
  margin-top: 10px;
  text-align: center;
  color: #666;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-input {
  width: 200px;
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
}

.product-info h3 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #333;
}

.description {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  height: 40px;
  overflow: hidden;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.price {
  color: #e65d17;
  font-size: 18px;
  font-weight: bold;
}

.sales {
  color: #999;
  font-size: 12px;
}

.button-group {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .products {
    flex-direction: column;
  }

  .filter-section {
    width: 100%;
  }

  .toolbar {
    flex-direction: column;
    gap: 10px;
  }

  .search-input {
    width: 100%;
  }
}
</style> 
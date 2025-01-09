<template>
  <div class="admin-products">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>

    <!-- 商品列表 -->
    <el-table
      v-loading="loading"
      :data="products"
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="商品图片" width="120">
        <template #default="{ row }">
          <el-image
            :src="row.imageUrl"
            :preview-src-list="[row.imageUrl]"
            fit="cover"
            style="width: 80px; height: 80px"
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
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="price" label="价格" width="120">
        <template #default="{ row }">
          ¥{{ row.price.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="120" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button 
              :type="row.status === 'ON_SALE' ? 'danger' : 'success'" 
              link 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'ON_SALE' ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑商品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingProduct ? '编辑商品' : '添加商品'"
      width="60%"
    >
      <el-form
        ref="formRef"
        :model="productForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" />
        </el-form-item>
        <el-form-item label="商品图片" prop="imageUrl">
          <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <el-image
              v-if="productForm.imageUrl"
              :src="productForm.imageUrl"
              class="avatar"
              @error="(e) => productForm.imageUrl = DEFAULT_IMAGE"
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
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="productForm.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="商品库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" rows="4" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture, Loading } from '@element-plus/icons-vue'
import { getProducts, createProduct, updateProduct, deleteProduct } from '@/api/product'
import { getCategories } from '@/api/category'

// 数据列表
const products = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表单相关
const dialogVisible = ref(false)
const editingProduct = ref(null)
const categories = ref([])
const productForm = ref({
  name: '',
  imageUrl: '',
  price: 0,
  stock: 0,
  description: '',
  categoryId: null
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }]
}

// 获取商品列表
const loadProducts = async () => {
  try {
    loading.value = true
    const response = await getProducts({
      page: currentPage.value - 1,
      size: pageSize.value
    })
    if (response.code === 200) {
      products.value = response.data.content
      total.value = response.data.totalElements
    }
  } catch (error) {
    console.error('Failed to load products:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const loadCategories = async () => {
  try {
    const response = await getCategories()
    if (response.code === 200) {
      categories.value = response.data
    }
  } catch (error) {
    console.error('Failed to load categories:', error)
    ElMessage.error('加载分类列表失败')
  }
}

// 处理添加商品
const handleAdd = () => {
  editingProduct.value = null
  productForm.value = {
    name: '',
    imageUrl: '',
    price: 0,
    stock: 0,
    description: '',
    categoryId: null
  }
  dialogVisible.value = true
}

// 处理编辑商品
const handleEdit = (row) => {
  editingProduct.value = row
  productForm.value = {
    id: row.id,
    name: row.name,
    imageUrl: row.imageUrl,
    price: row.price,
    stock: row.stock,
    description: row.description,
    status: row.status,
    categoryId: row.category?.id || null
  }
  dialogVisible.value = true
}

// 处理保存
const handleSave = async () => {
  try {
    // 如果没有上传图片，使用默认图片
    if (!productForm.value.imageUrl) {
      productForm.value.imageUrl = getRandomDefaultImage()
    }

    // 构建提交的数据
    const submitData = {
      ...productForm.value,
      category: productForm.value.categoryId ? { id: productForm.value.categoryId } : null
    }
    // 删除不需要的字段
    delete submitData.categoryId
    delete submitData.createdAt
    delete submitData.updatedAt

    const api = editingProduct.value ? updateProduct : createProduct
    const response = await api(submitData)
    if (response.code === 200) {
      ElMessage.success(editingProduct.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadProducts()
    }
  } catch (error) {
    console.error('Failed to save product:', error)
    ElMessage.error(editingProduct.value ? '更新失败' : '添加失败')
  }
}

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该商品吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await deleteProduct(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        loadProducts()
      }
    } catch (error) {
      console.error('Failed to delete product:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 处理上架/下架
const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === 'ON_SALE' ? 'OFF_SHELF' : 'ON_SALE'
    // 保持原有字段不变，只修改status
    const updateData = {
      id: row.id,
      name: row.name,
      description: row.description,
      price: row.price,
      stock: row.stock,
      category: {
        id: row.category?.id
      },
      status: newStatus,
      imageUrl: row.imageUrl
    }
    const response = await updateProduct(updateData)
    if (response.code === 200) {
      ElMessage.success(newStatus === 'ON_SALE' ? '上架成功' : '下架成功')
      loadProducts()
    }
  } catch (error) {
    console.error('Failed to toggle product status:', error)
    ElMessage.error('操作失败')
  }
}

// 处理图片上传
const handleUploadSuccess = (response) => {
  productForm.value.imageUrl = response.data
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  return true
}

// 分页相关
const handleSizeChange = (val) => {
  pageSize.value = val
  loadProducts()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadProducts()
}

const defaultImages = [
  'https://img.freepik.com/free-photo/chinese-tea-ceremony_23-2147999067.jpg',
  'https://img.freepik.com/free-photo/traditional-chinese-tea-ceremony-arrangement_23-2149017096.jpg',
  'https://img.freepik.com/free-photo/green-tea-white-ceramic-cup_1150-9284.jpg',
  'https://img.freepik.com/free-photo/chinese-tea-ceremony-arrangement_23-2148999035.jpg',
  'https://img.freepik.com/free-photo/tea-concept-with-copy-space_23-2148554168.jpg'
]

// 获取随机默认图片
const getRandomDefaultImage = () => {
  const index = Math.floor(Math.random() * defaultImages.length)
  return defaultImages[index]
}

// 在 script setup 部分添加默认图片常量
const DEFAULT_IMAGE = 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'

// 处理图片加载失败
const handleImageError = (e) => {
  const img = e.target
  if (img) {
    img.src = DEFAULT_IMAGE
  }
}

// 在 script setup 部分添加状态相关的工具函数
const getStatusType = (status) => {
  const types = {
    'ON_SALE': 'success',
    'OFF_SHELF': 'info',
    'SOLD_OUT': 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'ON_SALE': '在售',
    'OFF_SHELF': '下架',
    'SOLD_OUT': '售罄'
  }
  return texts[status] || status
}

onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>

<style scoped>
.admin-products {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
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
  font-size: 24px;
}
</style> 
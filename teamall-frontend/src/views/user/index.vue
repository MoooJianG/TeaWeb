<template>
  <div class="user-center">
    <el-card class="user-info-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" link @click="startEdit" v-if="!isEditing">
            编辑资料
          </el-button>
          <div v-else>
            <el-button type="primary" @click="saveUserInfo" :loading="loading">
              保存
            </el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </div>
        </div>
      </template>

      <div class="user-info">
        <div class="avatar-container">
          <el-avatar 
            :size="100" 
            :src="userInfo.avatarUrl || defaultAvatar"
            @error="() => userInfo.avatarUrl = defaultAvatar"
          />
          <el-upload
            v-if="isEditing"
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-button type="primary" link>更换头像</el-button>
          </el-upload>
        </div>

        <div class="info-form">
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            :disabled="!isEditing"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" disabled />
            </el-form-item>
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-form>
          <div class="action-buttons">
            <el-button type="warning" @click="showChangePasswordDialog">
              修改密码
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="30%"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-card class="order-card">
      <template #header>
        <div class="card-header">
          <span>我的订单</span>
          <el-button type="primary" link @click="$router.push('/order/list')">
            查看全部订单
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部" name="all">
          <el-empty v-if="visibleOrders.length === 0" description="暂无订单" />
          <order-list v-else :orders="visibleOrders" @refresh="fetchOrders" @delete="handleDelete" />
        </el-tab-pane>
        <el-tab-pane label="待付款" name="unpaid">
          <el-empty v-if="unpaidOrders.length === 0" description="暂无待付款订单" />
          <order-list v-else :orders="unpaidOrders" @refresh="fetchOrders" @delete="handleDelete" />
        </el-tab-pane>
        <el-tab-pane label="待发货" name="unshipped">
          <el-empty v-if="unshippedOrders.length === 0" description="暂无待发货订单" />
          <order-list v-else :orders="unshippedOrders" @refresh="fetchOrders" @delete="handleDelete" />
        </el-tab-pane>
        <el-tab-pane label="待收货" name="shipped">
          <el-empty v-if="shippedOrders.length === 0" description="暂无待收货订单" />
          <order-list v-else :orders="shippedOrders" @refresh="fetchOrders" @delete="handleDelete" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card class="address-card">
      <template #header>
        <div class="card-header">
          <span>收货地址</span>
          <el-button type="primary" link @click="$router.push('/addresses')">
            管理收货地址
          </el-button>
        </div>
      </template>

      <div class="address-list">
        <el-empty v-if="addresses.length === 0" description="暂无收货地址" />
        <div v-else class="address-item" v-for="address in addresses" :key="address.id">
          <div class="address-info">
            <p>
              <span class="name">{{ address.receiverName }}</span>
              <span class="phone">{{ address.receiverPhone }}</span>
              <el-tag v-if="address.isDefault" size="small" type="success">默认</el-tag>
            </p>
            <p class="address-detail">
              {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
            </p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserInfo, updateUserInfo, updatePassword } from '@/api/user'
import { getOrders } from '@/api/order'
import { getAddressList } from '@/api/address'
import OrderList from '@/components/OrderList.vue'
import { useRoute } from 'vue-router'

const defaultAvatar = 'https://s2.loli.net/2024/12/29/OND2VzYakGlSRHP.jpg'
const isEditing = ref(false)
const loading = ref(false)
const formRef = ref(null)
const activeTab = ref('all')
const userInfo = ref({})

// 用户信息表单
const form = reactive({
  username: '',
  email: '',
  phone: '',
  avatarUrl: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 订单和地址数据
const orders = ref([])
const addresses = ref([])

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

// 计算不同状态的订单
const unpaidOrders = computed(() => 
  visibleOrders.value.filter(order => order.status === 'PENDING_PAYMENT')
)
const unshippedOrders = computed(() => 
  visibleOrders.value.filter(order => order.status === 'PENDING_DELIVERY')
)
const shippedOrders = computed(() => 
  visibleOrders.value.filter(order => order.status === 'PENDING_RECEIPT')
)

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const response = await getUserInfo()
    if (response.data) {
      userInfo.value = response.data
      Object.assign(form, response.data)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 获取订单列表
const fetchOrders = async () => {
  try {
    const response = await getOrders()
    console.log('个人中心订单数据:', response)
    if (response.code === 200 && response.data && response.data.content) {
      orders.value = response.data.content
    } else {
      orders.value = []
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    orders.value = []
    ElMessage.error('获取订单列表失败')
  }
}

// 监听路由变化
const route = useRoute()

// 监听路由变化，当从地址管理页面返回时刷新地址列表
watch(
  () => route.path,
  (newPath, oldPath) => {
    if (oldPath === '/addresses' && newPath === '/user') {
      fetchAddresses()
    }
  }
)

// 获取地址列表
const fetchAddresses = async () => {
  try {
    const response = await getAddressList()
    if (response.code === 200) {
      addresses.value = response.data
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
    ElMessage.error('获取地址列表失败')
  }
}

// 开始编辑
const startEdit = () => {
  isEditing.value = true
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  fetchUserInfo() // 重新获取用户信息
}

// 保存用户信息
const saveUserInfo = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 确保发送用户名，即使没有修改
    const updateData = {
      username: form.username != '' ? form.username : userInfo.value.username,
      phone: form.phone,
      avatarUrl: form.avatarUrl
    }
    
    const response = await updateUserInfo(updateData)
    userInfo.value = response.data
    ElMessage.success('保存成功')
    isEditing.value = false
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    loading.value = false
  }
}

// 头像上传相关方法
const handleAvatarSuccess = (response) => {
  form.avatarUrl = response.url
  ElMessage.success('头像上传成功')
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

// 修改密码相关
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 显示修改密码对话框
const showChangePasswordDialog = () => {
  passwordDialogVisible.value = true
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

// 处理修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        passwordLoading.value = true
        await updatePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        })
        ElMessage.success('密码修改成功')
        passwordDialogVisible.value = false
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error(error.response?.data?.message || '修改密码失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
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

// 添加自动刷新功能
const setupAutoRefresh = () => {
  // 每30秒刷新一次地址列表
  const refreshInterval = setInterval(() => {
    if (route.path === '/user') {
      fetchAddresses()
    }
  }, 30000)

  // 组件卸载时清除定时器
  onUnmounted(() => {
    clearInterval(refreshInterval)
  })
}

// 页面加载时获取数据
onMounted(() => {
  fetchUserInfo()
  fetchOrders()
  fetchAddresses()
  setupAutoRefresh()
})
</script>

<style scoped>
.user-center {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  gap: 30px;
  padding: 20px;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.info-form {
  flex: 1;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: flex-start;
  gap: 10px;
}

.order-card,
.address-card {
  margin-top: 20px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.address-item {
  border: 1px solid #eee;
  padding: 20px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.address-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.address-info {
  .name {
    font-weight: bold;
    margin-right: 10px;
    font-size: 16px;
  }
  
  .phone {
    color: #666;
  }

  .address-detail {
    margin-top: 8px;
    color: #666;
    line-height: 1.5;
  }
}

@media (max-width: 768px) {
  .user-info {
    flex-direction: column;
    align-items: center;
  }
  
  .info-form {
    width: 100%;
  }
  
  .address-list {
    padding: 10px;
  }
  
  .address-item {
    padding: 15px;
  }
}
</style> 
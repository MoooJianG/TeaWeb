<template>
  <div class="settlement-container">
    <div class="settlement-content">
      <!-- 收货地址 -->
      <div class="address-section">
        <h3>收货地址</h3>
        <div v-if="selectedAddress" class="selected-address">
          <div class="address-info">
            <div class="receiver-info">
              <span class="name">{{ selectedAddress.receiverName }}</span>
              <span class="phone">{{ selectedAddress.receiverPhone }}</span>
            </div>
            <div class="address-detail">
              {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }}{{ selectedAddress.detailAddress }}
            </div>
          </div>
          <el-button type="primary" link @click="showAddressDialog">更换地址</el-button>
        </div>
        <el-button v-else type="primary" @click="showAddressDialog">添加收货地址</el-button>
      </div>

      <!-- 商品信息 -->
      <div class="products-section">
        <h3>商品信息</h3>
        <div class="product-list">
          <div v-for="item in orderItems" :key="item.productId" class="product-item">
            <el-image :src="item.imageUrl" :alt="item.name" class="product-image" />
            <div class="product-info">
              <h4>{{ item.name }}</h4>
              <p class="price">￥{{ item.price }}</p>
              <p class="quantity">x {{ item.quantity }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单金额 -->
      <div class="order-amount">
        <div class="amount-item">
          <span>商品总额</span>
          <span class="price">￥{{ totalAmount }}</span>
        </div>
        <div class="amount-item">
          <span>运费</span>
          <span class="price">￥{{ shippingFee }}</span>
        </div>
        <div class="amount-item total">
          <span>实付金额</span>
          <span class="price">￥{{ finalAmount }}</span>
        </div>
      </div>

      <!-- 提交订单 -->
      <div class="submit-section">
        <el-button type="primary" :loading="submitting" @click="submitOrder">
          提交订单
        </el-button>
      </div>
    </div>

    <!-- 地址管理对话框 -->
    <el-dialog
      v-model="addressDialogVisible"
      title="选择收货地址"
      width="800px"
      destroy-on-close
    >
      <div class="address-list">
        <el-empty v-if="addresses.length === 0" description="暂无收货地址">
          <el-button type="primary" @click="$router.push('/addresses')">去添加</el-button>
        </el-empty>
        <div
          v-else
          v-for="address in addresses"
          :key="address.id"
          class="address-item"
          :class="{ 'is-selected': selectedAddress?.id === address.id }"
          @click="handleSelectAddress(address)"
        >
          <div class="address-content">
            <div class="receiver-info">
              <span class="name">{{ address.receiverName }}</span>
              <span class="phone">{{ address.receiverPhone }}</span>
              <el-tag v-if="address.isDefault" size="small" type="success">默认</el-tag>
            </div>
            <div class="address-detail">
              {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addressDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAddress">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAddressList } from '@/api/address'
import { createOrder } from '@/api/order'
import AddressManagement from '@/views/address/index.vue'

const router = useRouter()
const route = useRoute()

// 状态变量
const addresses = ref([])
const selectedAddress = ref(null)
const addressDialogVisible = ref(false)
const submitting = ref(false)
const orderItems = ref([])

// 计算属性
const totalAmount = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const shippingFee = computed(() => {
  // 这里可以根据实际业务逻辑计算运费
  return 0
})

const finalAmount = computed(() => {
  return totalAmount.value + shippingFee.value
})

// 获取地址列表
const fetchAddresses = async () => {
  try {
    const response = await getAddressList()
    if (response.code === 200) {
      addresses.value = response.data
      // 如果有默认地址，则选中默认地址
      const defaultAddress = addresses.value.find(addr => addr.isDefault)
      if (defaultAddress) {
        selectedAddress.value = defaultAddress
      }
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
    ElMessage.error('获取地址列表失败')
  }
}

// 跳转到地址管理页面
const goToAddressManagement = () => {
  router.push({
    path: '/address',
    query: {
      redirect: route.fullPath // 保存当前页面路径，便于地址管理完成后返回
    }
  })
}

// 显示地址选择对话框
const showAddressDialog = () => {
  addressDialogVisible.value = true
}

// 选择地址
const selectAddress = (address) => {
  selectedAddress.value = address
  addressDialogVisible.value = false
}

// 提交订单
const submitOrder = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  try {
    submitting.value = true
    const orderData = {
      addressId: selectedAddress.value.id,
      items: orderItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    }

    const response = await createOrder(orderData)
    if (response.code === 200) {
      ElMessage.success('订单创建成功')
      // 跳转到订单详情页面
      router.push({
        path: `/order/detail/${response.data.id}`
      })
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 处理地址选择
const handleSelectAddress = (address) => {
  selectedAddress.value = address
  addressDialogVisible.value = false
}

// 初始化
onMounted(() => {
  // 从路由参数中获取商品信息
  const items = route.query.items
  if (items) {
    try {
      orderItems.value = JSON.parse(items)
      if (!orderItems.value.length) {
        ElMessage.error('商品信息不完整')
        router.push('/')
        return
      }
    } catch (error) {
      console.error('解析商品信息失败:', error)
      ElMessage.error('商品信息无效')
      router.push('/')
    }
  } else {
    ElMessage.error('未找到商品信息')
    router.push('/')
  }

  // 获取地址列表
  fetchAddresses()
})
</script>

<style scoped>
.settlement-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.settlement-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.address-section,
.products-section,
.order-amount {
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.address-section h3,
.products-section h3 {
  margin: 0 0 20px;
  font-size: 18px;
  color: #333;
}

.selected-address {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.address-info .contact {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.address-info .address {
  color: #666;
}

.no-address {
  text-align: center;
  padding: 30px;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin: 0 0 8px;
  font-size: 16px;
}

.price {
  color: #f56c6c;
  font-weight: 500;
}

.quantity {
  color: #666;
}

.order-amount {
  padding: 20px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.amount-item.total {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  font-size: 18px;
  font-weight: 500;
}

.submit-section {
  padding: 20px;
  text-align: right;
}

.submit-section .el-button {
  width: 200px;
  height: 45px;
  font-size: 16px;
}

.address-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
}

.address-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.address-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.address-item.is-selected {
  border-color: #409EFF;
  background-color: #ecf5ff;
}

.address-content {
  .receiver-info {
    margin-bottom: 8px;
    
    .name {
      font-weight: bold;
      font-size: 16px;
      margin-right: 15px;
    }
    
    .phone {
      color: #666;
      margin-right: 15px;
    }
  }
  
  .address-detail {
    color: #333;
    line-height: 1.5;
  }
}

.add-address {
  text-align: center;
  padding: 20px;
}

@media (max-width: 768px) {
  .settlement-container {
    padding: 10px;
  }

  .product-image {
    width: 60px;
    height: 60px;
  }

  .submit-section .el-button {
    width: 100%;
  }
}
</style> 
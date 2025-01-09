<template>
  <div class="address-container">
    <div class="header">
      <h2>收货地址管理</h2>
      <el-button type="primary" @click="showAddressDialog()">新增地址</el-button>
    </div>

    <div class="address-list">
      <el-empty v-if="addresses.length === 0" description="暂无收货地址" />
      
      <div v-else class="address-item" v-for="address in addresses" :key="address.id">
        <div class="address-info" @click="handleSelect(address)">
          <div class="name-phone">
            <span class="name">{{ address.receiverName }}</span>
            <span class="phone">{{ address.receiverPhone }}</span>
            <el-tag v-if="address.isDefault" size="small" type="success">默认地址</el-tag>
          </div>
          <div class="detail">
            {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
          </div>
        </div>
        
        <div class="address-actions">
          <el-button 
            v-if="!address.isDefault" 
            type="primary" 
            link 
            @click="handleSetDefault(address.id)"
          >
            设为默认
          </el-button>
          <el-button type="primary" link @click="showAddressDialog(address)">
            编辑
          </el-button>
          <el-button 
            type="danger" 
            link 
            @click="handleDelete(address)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 地址编辑对话框 -->
    <el-dialog
      :title="currentAddress.id ? '编辑地址' : '新增地址'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        
        <el-form-item label="手机号码" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号码" />
        </el-form-item>
        
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择所在地区"
          />
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input
            v-model="addressForm.detailAddress"
            type="textarea"
            rows="3"
            placeholder="请输入详细地址，如街道、门牌号等"
          />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认收货地址</el-checkbox>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, defineEmits } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address'

const emit = defineEmits(['select-address'])

const addresses = ref([])
const dialogVisible = ref(false)
const loading = ref(false)
const addressFormRef = ref(null)
const currentAddress = ref({})

// 表单数据
const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detailAddress: '',
  isDefault: false
})

// 表单验证规则
const rules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 100, message: '长度在 5 到 100 个字符', trigger: 'blur' }
  ]
}

// 地区选项
const regionOptions = [
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' },
          { value: '朝阳区', label: '朝阳区' },
          { value: '海淀区', label: '海淀区' },
          { value: '丰台区', label: '丰台区' },
          { value: '石景山区', label: '石景山区' },
          { value: '门头沟区', label: '门头沟区' },
          { value: '房山区', label: '房山区' },
          { value: '通州区', label: '通州区' },
          { value: '顺义区', label: '顺义区' },
          { value: '昌平区', label: '昌平区' },
          { value: '大兴区', label: '大兴区' },
          { value: '怀柔区', label: '怀柔区' },
          { value: '平谷区', label: '平谷区' }
        ]
      }
    ]
  },
  {
    value: '重庆市',
    label: '重庆市',
    children: [
      {
        value: '重庆市',
        label: '重庆市',
        children: [
          { value: '渝中区', label: '渝中区' },
          { value: '江北区', label: '江北区' },
          { value: '南岸区', label: '南岸区' },
          { value: '九龙坡区', label: '九龙坡区' },
          { value: '沙坪坝区', label: '沙坪坝区' }
        ]
      }
    ]
  },
  {
    value: '四川省',
    label: '四川省',
    children: [
      {
        value: '成都市',
        label: '成都市',
        children: [
          { value: '锦江区', label: '锦江区' },
          { value: '青羊区', label: '青羊区' },
          { value: '金牛区', label: '金牛区' },
          { value: '武侯区', label: '武侯区' },
          { value: '成华区', label: '成华区' }
        ]
      },
      {
        value: '绵阳市',
        label: '绵阳市',
        children: [
          { value: '涪城区', label: '涪城区' },
          { value: '游仙区', label: '游仙区' },
          { value: '安州区', label: '安州区' }
        ]
      }
    ]
  },
  {
    value: '云南省',
    label: '云南省',
    children: [
      {
        value: '昆明市',
        label: '昆明市',
        children: [
          { value: '五华区', label: '五华区' },
          { value: '盘龙区', label: '盘龙区' },
          { value: '官渡区', label: '官渡区' },
          { value: '西山区', label: '西山区' }
        ]
      },
      {
        value: '大理白族自治州',
        label: '大理白族自治州',
        children: [
          { value: '大理市', label: '大理市' },
          { value: '漾濞县', label: '漾濞县' },
          { value: '祥云县', label: '祥云县' }
        ]
      }
    ]
  },
  {
    value: '广西壮族自治区',
    label: '广西壮族自治区',
    children: [
      {
        value: '南宁市',
        label: '南宁市',
        children: [
          { value: '青秀区', label: '青秀区' },
          { value: '兴宁区', label: '兴宁区' },
          { value: '西乡塘区', label: '西乡塘区' }
        ]
      },
      {
        value: '桂林市',
        label: '桂林市',
        children: [
          { value: '秀峰区', label: '秀峰区' },
          { value: '叠彩区', label: '叠彩区' },
          { value: '象山区', label: '象山区' }
        ]
      }
    ]
  },
]

// 获取地址列表
const fetchAddresses = async () => {
  try {
    const response = await getAddressList()
    if (response.code === 200) {
      addresses.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取地址列表失败')
  }
}

// 显示地址编辑对话框
const showAddressDialog = (address = null) => {
  currentAddress.value = address || {}
  if (address) {
    // 编辑模式：填充表单数据
    Object.assign(addressForm, {
      receiverName: address.receiverName,
      receiverPhone: address.receiverPhone,
      region: [address.province, address.city, address.district],
      detailAddress: address.detailAddress,
      isDefault: address.isDefault
    })
  } else {
    // 新增模式：重置表单
    Object.assign(addressForm, {
      receiverName: '',
      receiverPhone: '',
      region: [],
      detailAddress: '',
      isDefault: false
    })
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!addressFormRef.value) return
  
  try {
    await addressFormRef.value.validate()
    loading.value = true
    
    const addressData = {
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      province: addressForm.region[0],
      city: addressForm.region[1],
      district: addressForm.region[2],
      detailAddress: addressForm.detailAddress,
      isDefault: addressForm.isDefault
    }
    
    if (currentAddress.value.id) {
      // 更新地址
      await updateAddress(currentAddress.value.id, addressData)
      ElMessage.success('地址更新成功')
    } else {
      // 新增地址
      await addAddress(addressData)
      ElMessage.success('地址添加成功')
    }
    
    dialogVisible.value = false
    fetchAddresses()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 删除地址
const handleDelete = async (address) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteAddress(address.id)
    ElMessage.success('地址删除成功')
    fetchAddresses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 设置默认地址
const handleSetDefault = async (addressId) => {
  try {
    await setDefaultAddress(addressId)
    ElMessage.success('默认地址设置成功')
    await fetchAddresses() // 重新获取地址列表以更新状态
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error(error.response?.data?.message || '设置默认地址失败')
  }
}

// 处理地址选择
const handleSelect = (address) => {
  emit('select-address', {
    id: address.id,
    name: address.receiverName,
    phone: address.receiverPhone,
    province: address.province,
    city: address.city,
    district: address.district,
    detail: address.detailAddress,
    isDefault: address.isDefault
  })
}

// 页面加载时获取地址列表
onMounted(() => {
  fetchAddresses()
})
</script>

<style scoped>
.address-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #333;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-item {
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.address-info {
  flex: 1;
}

.name-phone {
  margin-bottom: 10px;
}

.name {
  font-weight: bold;
  margin-right: 15px;
}

.phone {
  color: #666;
  margin-right: 15px;
}

.detail {
  color: #666;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 15px;
}

@media (max-width: 768px) {
  .address-item {
    flex-direction: column;
  }
  
  .address-actions {
    margin-top: 15px;
    justify-content: flex-end;
  }
}
</style> 
<template>
  <div class="not-found">
    <h2>404 - 页面不存在</h2>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found {
  padding: 100px 20px;
  text-align: center;
}
</style> 
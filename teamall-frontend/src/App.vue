<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

onMounted(async () => {
  // 检查并恢复登录状态
  await userStore.checkLoginState()
})
</script>

<style>
#app {
  font-family: 'PingFang SC', 'Helvetica Neue', Helvetica, 'microsoft yahei', arial, STHeiTi, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
}
</style>

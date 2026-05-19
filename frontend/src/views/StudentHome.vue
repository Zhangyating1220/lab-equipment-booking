<template>
  <div>
    <!-- 顶部导航菜单 -->
    <el-menu mode="horizontal" router>
      <el-menu-item index="/student/equipment">设备列表</el-menu-item>
      <el-menu-item index="/student/my-bookings">我的预约</el-menu-item>
      <el-menu-item @click="logout" style="float: right">退出登录</el-menu-item>
    </el-menu>

    <!-- 子路由内容区域 -->
    <div style="padding: 20px">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 退出登录
const logout = () => {
  localStorage.clear()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
</style>
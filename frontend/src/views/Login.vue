<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>实验室设备预约系统</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入学号/工号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" v-model="form.password" placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  try {
    const res = await request.post('/api/user/login', form)
    if (res.code === 200) {
      localStorage.setItem('token', res.token)
      localStorage.setItem('role', res.role)
      localStorage.setItem('name', res.name)
      ElMessage.success('登录成功')
      if (res.role === 0) {
        router.push('/student')
      } else {
        router.push('/admin')
      }
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error('登录失败', error)
    ElMessage.error('登录失败，请稍后再试')
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
}
</style>
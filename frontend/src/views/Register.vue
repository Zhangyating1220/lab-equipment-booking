<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>用户注册</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入学号/工号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" v-model="form.password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话（可选）" />
        </el-form-item>
        <el-button type="primary" @click="handleRegister">注册</el-button>
        <el-button @click="goToLogin">返回登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const form = reactive({
  username: '',
  password: '',
  name: '',
  phone: ''
})

const handleRegister = async () => {
  if (!form.username || !form.password || !form.name) {
    ElMessage.warning('请填写账号、密码和姓名')
    return
  }
  try {
    const res = await request.post('/user/register', form)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败', error)
    ElMessage.error('注册失败，请稍后再试')
  }
}

const goToLogin = () => {
  router.push('/login')
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
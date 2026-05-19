<template>
  <div class="my-bookings">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的预约</span>
          <el-button type="primary" size="small" @click="loadBookings" :loading="loading">刷新</el-button>
        </div>
      </template>

      <el-table :data="bookings" v-loading="loading" stripe>
        <el-table-column prop="equipmentName" label="设备名称" min-width="150" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="事由/拒绝原因" min-width="150" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="danger" size="small" @click="cancelBooking(row.id)">取消</el-button>
            <span v-else>--</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && bookings.length === 0" description="暂无预约记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const bookings = ref([])
const loading = ref(false)

const getStatusText = (status) => {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝', 3: '已取消' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || ''
}

const loadBookings = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    return
  }
  const user = JSON.parse(userStr)
  loading.value = true
  try {
    const res = await request.get('/booking/my', { params: { userId: user.id } })
    bookings.value = res || []
  } catch (error) {
    console.error('加载预约列表失败', error)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const cancelBooking = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', { type: 'warning' })
    const userStr = localStorage.getItem('user')
    const user = JSON.parse(userStr)
    const res = await request.post(`/booking/cancel/${id}`, null, { params: { userId: user.id } })
    if (res.success) {
      ElMessage.success('取消成功')
      loadBookings()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

onMounted(() => {
  loadBookings()
})
</script>

<style scoped>
.my-bookings {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
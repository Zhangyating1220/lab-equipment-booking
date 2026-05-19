<template>
  <div>
    <h2>我的预约</h2>
    <el-table :data="bookings" style="width:100%" v-loading="loading">
      <el-table-column prop="equipmentName" label="设备名称" />
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="拒绝原因" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button 
            v-if="row.status === 0"
            type="danger" 
            size="small" 
            @click="cancelBooking(row.id)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>
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
  if (!userStr) return
  const user = JSON.parse(userStr)
  loading.value = true
  try {
    const res = await request.get('/booking/my', { params: { userId: user.id } })
    bookings.value = res || []
  } catch (error) {
    console.error('加载预约列表失败', error)
    ElMessage.error('加载失败')
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
      ElMessage.error(res.message)
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
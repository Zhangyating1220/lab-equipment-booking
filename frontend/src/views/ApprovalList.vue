<template>
  <div class="approval-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待审批预约</span>
          <el-button type="primary" size="small" @click="loadApprovals" :loading="loading">刷新</el-button>
        </div>
      </template>

      <el-table :data="approvals" v-loading="loading" stripe>
        <el-table-column prop="equipmentName" label="设备名称" min-width="150" />
        <el-table-column prop="userName" label="申请人" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="reason" label="预约事由" min-width="150" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleApprove(row.id)">通过</el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && approvals.length === 0" description="暂无待审批预约" />
    </el-card>

    <!-- 拒绝弹窗 -->
    <el-dialog title="拒绝预约" v-model="rejectDialogVisible" width="450px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input 
            type="textarea" 
            v-model="rejectForm.reason" 
            placeholder="请输入拒绝原因"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject" :loading="submitting">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const approvals = ref([])
const loading = ref(false)
const rejectDialogVisible = ref(false)
const submitting = ref(false)
const currentBookingId = ref(null)

const rejectForm = reactive({
  reason: ''
})

const loadApprovals = async () => {
  loading.value = true
  try {
    const res = await request.get('/booking/pending')
    approvals.value = res.data || []
  } catch (error) {
    console.error('加载待审批列表失败', error)
    ElMessage.error('加载待审批列表失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确定要通过这个预约吗？', '提示', { type: 'warning' })
    const res = await request.post(`/booking/approve/${id}`, { approve: true })
    if (res.success || res.code === 200) {
      ElMessage.success('审批通过')
      loadApprovals()
    } else {
      ElMessage.error(res.message || '审批失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审批失败')
    }
  }
}

const handleReject = (row) => {
  currentBookingId.value = row.id
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  submitting.value = true
  try {
    const res = await request.post(`/booking/approve/${currentBookingId.value}`, { 
      approve: false, 
      rejectReason: rejectForm.reason 
    })
    if (res.success || res.code === 200) {
      ElMessage.success('已拒绝预约')
      rejectDialogVisible.value = false
      loadApprovals()
    } else {
      ElMessage.error(res.message || '拒绝失败')
    }
  } catch (error) {
    console.error('拒绝预约失败', error)
    ElMessage.error('拒绝失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadApprovals()
})
</script>

<style scoped>
.approval-list {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
<template>
  <div class="approvals-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待审批列表</span>
          <el-button type="primary" size="small" @click="loadApprovals" :loading="loading">刷新</el-button>
        </div>
      </template>

      <el-table :data="approvals" v-loading="loading" stripe>
        <el-table-column prop="equipmentName" label="设备名称" min-width="150" />
        <el-table-column prop="userName" label="申请人" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="purpose" label="预约事由" min-width="150" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleApprove(row.id)">通过</el-button>
            <el-button type="danger" size="small" @click="handleReject(row.id)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && approvals.length === 0" description="暂无待审批预约" />
    </el-card>

    <!-- 拒绝理由弹窗 -->
    <el-dialog title="拒绝预约" v-model="rejectDialogVisible" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝理由" prop="reason">
          <el-input 
            type="textarea" 
            v-model="rejectForm.reason" 
            placeholder="请输入拒绝理由"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReject">确定拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const approvals = ref([])
const loading = ref(false)
const rejectDialogVisible = ref(false)
const rejectForm = reactive({ reason: '' })
const currentRejectId = ref(null)

const loadApprovals = async () => {
  loading.value = true
  try {
    const res = await request.get('/reservation/pending')

    if (res && res.code === 200) {
      approvals.value = res.data || []
    } else if (res) {
      approvals.value = res
    }
  } catch (error) {
    console.error('加载待审批列表失败', error)
    ElMessage.error('加载待审批列表失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = async (id) => {
  try {
    await request.put(`/reservation/approve/${id}`)
    ElMessage.success('审批通过')
    loadApprovals()
  } catch (error) {
    console.error('审批失败', error)
    ElMessage.error('审批失败')
  }
}

const handleReject = (id) => {
  currentRejectId.value = id
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入拒绝理由')
    return
  }
  try {
    await request.put(`/reservation/reject/${currentRejectId.value}`, {
      reason: rejectForm.reason.trim()
    })
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadApprovals()
  } catch (error) {
    console.error('拒绝失败', error)
    ElMessage.error('拒绝失败')
  }
}

onMounted(() => {
  loadApprovals()
})
</script>

<style scoped>
.approvals-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
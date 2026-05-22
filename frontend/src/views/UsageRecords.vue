<template>
  <div class="usage-records">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalRecords }}</div>
          <div class="stat-label">总使用次数</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon">⏱️</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalHours }}h</div>
          <div class="stat-label">总使用时长</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon">🔧</div>
        <div class="stat-info">
          <div class="stat-value">{{ equipmentCount }}</div>
          <div class="stat-label">设备数量</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ userCount }}</div>
          <div class="stat-label">使用人数</div>
        </div>
      </el-card>
    </div>

    <!-- 统计图表区域 -->
    <el-card class="chart-card">
      <template #header>
        <span>设备使用统计</span>
      </template>
      <div class="chart-content">
        <div v-for="item in equipmentStats" :key="item.equipmentName" class="bar-item">
          <div class="bar-label">{{ item.equipmentName }}</div>
          <div class="bar-container">
            <div 
              class="bar-fill" 
              :style="{ width: getBarWidth(item.usageCount) }"
            >{{ item.usageCount }}次</div>
          </div>
          <div class="bar-hint">时长: {{ formatMinutes(item.totalMinutes) }}</div>
        </div>
      </div>
    </el-card>

    <!-- 使用记录表格 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>使用记录列表</span>
          <el-button type="primary" size="small" @click="loadRecords" :loading="loading">刷新</el-button>
        </div>
      </template>

      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="equipment_name" label="设备名称" min-width="150" />
        <el-table-column prop="user_name" label="使用者" width="100" />
        <el-table-column prop="start_time" label="预约开始" width="180" />
        <el-table-column prop="end_time" label="预约结束" width="180" />
        <el-table-column prop="actual_start_time" label="实际开始" width="180" />
        <el-table-column prop="actual_end_time" label="实际结束" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              size="small" 
              @click="handleStart(row.id)"
            >开始使用</el-button>
            <el-button 
              v-if="row.status === 1" 
              type="warning" 
              size="small" 
              @click="handleEnd(row.id)"
            >结束使用</el-button>
            <span v-else>--</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && records.length === 0" description="暂无使用记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const records = ref([])
const equipmentStats = ref([])
const userStats = ref([])
const loading = ref(false)

const totalRecords = computed(() => {
  return records.value.length
})

const totalHours = computed(() => {
  let total = 0
  records.value.forEach(r => {
    if (r.actual_start_time) {
      const start = new Date(r.actual_start_time)
      const end = r.actual_end_time ? new Date(r.actual_end_time) : new Date()
      total += (end - start) / (1000 * 60 * 60)
    }
  })
  return total.toFixed(1)
})

const equipmentCount = computed(() => {
  return new Set(records.value.map(r => r.equipment_id)).size
})

const userCount = computed(() => {
  return userStats.value.length
})

const getStatusText = (status) => {
  const map = { 0: '待使用', 1: '使用中', 2: '已完成', 3: '已超时' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'primary', 3: 'danger' }
  return map[status] || ''
}

const formatMinutes = (minutes) => {
  if (!minutes) return '0h'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return `${hours}h ${mins}m`
  }
  return `${mins}m`
}

const getBarWidth = (count) => {
  if (!equipmentStats.value.length) return '0%'
  const max = Math.max(...equipmentStats.value.map(e => e.usageCount))
  return `${(count / max) * 100}%`
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await request.get('/usage/list')
    // 响应拦截器已提取 data，直接使用即可
    if (Array.isArray(res)) {
      records.value = res
    }
  } catch (error) {
    console.error('加载使用记录失败', error)
    ElMessage.error('加载使用记录失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await request.get('/usage/stats/summary')
    // 响应拦截器已经提取了 data，直接使用即可
    if (res && typeof res === 'object') {
      // 将下划线命名转换为驼峰命名
      equipmentStats.value = res.equipmentStats?.map(item => ({
        equipmentName: item.equipment_name,
        usageCount: item.usage_count,
        totalMinutes: item.total_minutes
      })) || []
      userStats.value = res.userStats?.map(item => ({
        userName: item.user_name,
        usageCount: item.usage_count,
        totalMinutes: item.total_minutes
      })) || []
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const handleStart = async (id) => {
  try {
    await request.post(`/usage/start/${id}`)
    ElMessage.success('开始使用')
    loadRecords()
  } catch (error) {
    console.error('开始使用失败', error)
    ElMessage.error('操作失败')
  }
}

const handleEnd = async (id) => {
  try {
    await request.post(`/usage/end/${id}`)
    ElMessage.success('使用结束')
    loadRecords()
    loadStats()
  } catch (error) {
    console.error('结束使用失败', error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadRecords()
  loadStats()
})
</script>

<style scoped>
.usage-records {
  padding: 20px;
}
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}
.stat-icon {
  font-size: 36px;
  margin-right: 16px;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
}
.stat-label {
  font-size: 14px;
  color: #6b7280;
}
.chart-card {
  margin-bottom: 20px;
}
.chart-content {
  padding: 10px 0;
}
.bar-item {
  margin-bottom: 12px;
}
.bar-label {
  font-size: 14px;
  margin-bottom: 4px;
  color: #374151;
}
.bar-container {
  height: 24px;
  background-color: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #1d4ed8);
  color: white;
  padding-left: 8px;
  line-height: 24px;
  font-size: 12px;
  border-radius: 4px;
  transition: width 0.3s;
}
.bar-hint {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
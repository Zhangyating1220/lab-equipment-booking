<template>
  <div class="equipment-list">
    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="设备名称">
          <el-input v-model="filters.name" placeholder="请输入设备名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="设备类别">
          <el-select v-model="filters.category" placeholder="全部" clearable style="width: 120px">
            <el-option label="显微镜" value="显微镜" />
            <el-option label="离心机" value="离心机" />
            <el-option label="PCR仪" value="PCR仪" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="可用" :value="0" />
            <el-option label="维修中" :value="1" />
            <el-option label="已废弃" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 设备表格 -->
    <el-card class="table-card">
      <el-table :data="equipmentList" v-loading="loading" stripe>
        <el-table-column prop="name" label="设备名称" min-width="150" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="location" label="位置" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : row.status === 1 ? 'warning' : 'info'">
              {{ row.status === 0 ? '可用' : row.status === 1 ? '维修中' : '已废弃' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showBooking(row)" :disabled="row.status !== 0">
              预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadEquipments"
          @current-change="loadEquipments"
        />
      </div>
    </el-card>

    <BookingDialog v-model="dialogVisible" :equipment="currentEquipment" @success="loadEquipments" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import BookingDialog from '@/components/BookingDialog.vue'

const equipmentList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const dialogVisible = ref(false)
const currentEquipment = ref({})

const filters = reactive({
  name: '',
  category: '',
  status: ''
})

const loadEquipments = async () => {
  loading.value = true
  try {
    const res = await request.get('/equipment/list', {
      params: {
        page: page.value,
        size: size.value,
        name: filters.name || undefined,
        category: filters.category || undefined,
        status: filters.status !== '' ? filters.status : undefined
      }
    })
    equipmentList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载设备列表失败', error)
    ElMessage.error('加载设备列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadEquipments()
}

const resetFilters = () => {
  filters.name = ''
  filters.category = ''
  filters.status = ''
  handleSearch()
}

const showBooking = (equipment) => {
  currentEquipment.value = equipment
  dialogVisible.value = true
}

onMounted(() => {
  loadEquipments()
})
</script>

<style scoped>
.equipment-list {
  padding: 20px;
}
.filter-card {
  margin-bottom: 20px;
}
.table-card {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
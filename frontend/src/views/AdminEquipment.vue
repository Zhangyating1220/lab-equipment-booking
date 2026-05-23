<template>
  <div class="admin-equipment">
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
            <el-option label="电子天平" value="电子天平" />
            <el-option label="恒温培养箱" value="恒温培养箱" />
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
          <el-button type="success" @click="showAddDialog">新增设备</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 设备表格 -->
    <el-card class="table-card">
      <el-table :data="equipmentList" v-loading="loading" stripe>
        <el-table-column prop="name" label="设备名称" min-width="150" />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="location" label="位置" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : row.status === 1 ? 'warning' : 'info'">
              {{ row.status === 0 ? '可用' : row.status === 1 ? '维修中' : '已废弃' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
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

    <!-- 添加/编辑弹窗 -->
    <el-dialog 
      :title="isEdit ? '编辑设备' : '新增设备'" 
      v-model="dialogVisible" 
      width="500px"
    >
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="form.model" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="form.category" placeholder="请选择类别">
            <el-option label="显微镜" value="显微镜" />
            <el-option label="离心机" value="离心机" />
            <el-option label="PCR仪" value="PCR仪" />
            <el-option label="电子天平" value="电子天平" />
            <el-option label="恒温培养箱" value="恒温培养箱" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入设备位置" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="可用" :value="0" />
            <el-option label="维修中" :value="1" />
            <el-option label="已废弃" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" placeholder="请输入备注信息" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">{{ isEdit ? '保存修改' : '确认添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const equipmentList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const filters = reactive({
  name: '',
  category: '',
  status: ''
})

const form = reactive({
  id: null,
  name: '',
  model: '',
  category: '',
  location: '',
  status: 0,
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  model: [{ required: true, message: '请输入设备型号', trigger: 'blur' }],
  category: [{ required: true, message: '请选择设备类别', trigger: 'change' }],
  location: [{ required: true, message: '请输入设备位置', trigger: 'blur' }]
}

const loadEquipments = async () => {
  loading.value = true
  try {
    const res = await request.get('/equipment/list', {
      params: {
        pageNum: page.value,
        pageSize: size.value,
        name: filters.name || undefined,
        category: filters.category || undefined,
        status: filters.status !== '' ? filters.status : undefined
      }
    })
    // 响应拦截器已经提取了 data，直接使用即可
    if (res && res.records) {
      equipmentList.value = res.records
      total.value = res.total
    } else if (Array.isArray(res)) {
      equipmentList.value = res
      total.value = res.length
    } else {
      equipmentList.value = []
      total.value = 0
    }
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

const showAddDialog = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.model = ''
  form.category = ''
  form.location = ''
  form.status = 0
  form.remark = ''
  dialogVisible.value = true
}

const showEditDialog = (equipment) => {
  isEdit.value = true
  form.id = equipment.id
  form.name = equipment.name
  form.model = equipment.model
  form.category = equipment.category
  form.location = equipment.location
  form.status = equipment.status
  form.remark = equipment.remark || ''
  dialogVisible.value = true
}

const submitForm = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  try {
    if (isEdit.value) {
      await request.put('/equipment/update', form)
    } else {
      await request.post('/equipment/add', form)
    }
    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
    dialogVisible.value = false
    loadEquipments()
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error(isEdit.value ? '修改失败' : '添加失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该设备吗？', '提示', { type: 'warning' })
    await request.delete(`/equipment/delete/${id}`)
    ElMessage.success('删除成功')
    loadEquipments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadEquipments()
})
</script>

<style scoped>
.admin-equipment {
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
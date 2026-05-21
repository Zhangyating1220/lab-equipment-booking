<template>
  <el-dialog 
    title="预约设备" 
    v-model="visible" 
    width="500px" 
    @close="handleClose"
    destroy-on-close
  >
    <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
      <el-form-item label="设备名称">
        <span>{{ equipment.name }}</span>
      </el-form-item>
      <el-form-item label="设备位置">
        <span>{{ equipment.location || '未填写' }}</span>
      </el-form-item>
      <el-form-item label="预约日期" prop="date">
        <el-date-picker
          v-model="form.date"
          type="date"
          placeholder="选择预约日期"
          :disabled-date="disabledDate"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          @change="onDateChange"
        />
      </el-form-item>
      <el-form-item label="预约时段" prop="slot">
        <el-select 
          v-model="form.slot" 
          placeholder="请先选择日期" 
          :disabled="!form.date || loadingSlots"
          style="width: 100%"
          clearable
        >
          <el-option
            v-for="slot in availableSlots"
            :key="slot"
            :label="slot"
            :value="slot"
          />
        </el-select>
        <div v-if="form.date && availableSlots.length === 0 && !loadingSlots" class="tip">
          当天暂无可用时段
        </div>
        <div v-if="loadingSlots" class="tip">加载中...</div>
      </el-form-item>
      <el-form-item label="预约事由" prop="reason">
        <el-input 
          type="textarea" 
          v-model="form.reason" 
          placeholder="请输入预约事由（选填）"
          :rows="3"
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submitBooking" :loading="submitting">
        提交预约
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  modelValue: Boolean,
  equipment: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const submitting = ref(false)
const loadingSlots = ref(false)
const formRef = ref(null)
const availableSlots = ref([])

const form = reactive({
  date: '',
  slot: '',
  reason: ''
})

const rules = {
  date: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  slot: [{ required: true, message: '请选择预约时段', trigger: 'change' }]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (!val) {
    resetForm()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const resetForm = () => {
  form.date = ''
  form.slot = ''
  form.reason = ''
  availableSlots.value = []
  loadingSlots.value = false
  formRef.value?.resetFields()
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 86400000
}

const onDateChange = async () => {
  if (!form.date) return
  
  loadingSlots.value = true
  availableSlots.value = []
  form.slot = ''
  
  try {
    const res = await request.get('/booking/available-slots', {
      params: { 
        equipmentId: props.equipment.id, 
        date: form.date 
      }
    })
    availableSlots.value = res.data || []
  } catch (error) {
    console.error('获取时段失败', error)
    ElMessage.error('获取可用时段失败')
  } finally {
    loadingSlots.value = false
  }
}

const submitBooking = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    return
  }
  const user = JSON.parse(userStr)
  const userId = user.id || user.userId
  
  if (!userId) {
    ElMessage.warning('无法获取用户信息')
    return
  }
  
  const [startHour, endHour] = form.slot.split('-')
  const startTime = `${form.date} ${startHour}:00`
  const endTime = `${form.date} ${endHour}:00`
  
  submitting.value = true
  try {
    const res = await request.post('/booking/submit', {
      userId,
      equipmentId: props.equipment.id,
      startTime,
      endTime,
      reason: form.reason || ''
    })
    
    if (res.code === 200) {
      ElMessage.success('预约成功，等待管理员审批')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(res.message || '预约失败')
    }
  } catch (error) {
    console.error('预约失败', error)
    ElMessage.error('预约失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}
</style>
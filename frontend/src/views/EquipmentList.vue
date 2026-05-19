<template>
  <div class="equipment-list">
    <h2>设备列表</h2>
    <div class="equipment-grid">
      <div 
        v-for="equipment in equipmentList" 
        :key="equipment.id" 
        class="equipment-card"
      >
        <h3>{{ equipment.name }}</h3>
        <p>型号: {{ equipment.model }}</p>
        <p>状态: {{ equipment.status === 'available' ? '可预约' : '已预约' }}</p>
        <button 
          v-if="equipment.status === 'available'" 
          @click="bookEquipment(equipment.id)"
        >
          预约
        </button>
        <span v-else class="booked">已被预约</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const equipmentList = ref([
  { id: 1, name: '示波器', model: 'DSO-1000', status: 'available' },
  { id: 2, name: '信号发生器', model: 'SG-2000', status: 'available' },
  { id: 3, name: '万用表', model: 'DMM-3000', status: 'booked' },
  { id: 4, name: '电源供应器', model: 'PS-4000', status: 'available' },
  { id: 5, name: '频谱分析仪', model: 'SA-5000', status: 'available' },
  { id: 6, name: '逻辑分析仪', model: 'LA-6000', status: 'booked' },
])

const bookEquipment = (id) => {
  const equipment = equipmentList.value.find(e => e.id === id)
  if (equipment) {
    equipment.status = 'booked'
    alert(`已成功预约设备: ${equipment.name}`)
  }
}
</script>

<style scoped>
.equipment-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.equipment-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.equipment-card:hover {
  transform: translateY(-5px);
}

.equipment-card h3 {
  margin-bottom: 10px;
  color: #2c3e50;
}

.equipment-card p {
  margin: 5px 0;
  color: #666;
}

button {
  margin-top: 15px;
  padding: 10px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

button:hover {
  background: #2980b9;
}

.booked {
  display: inline-block;
  margin-top: 15px;
  padding: 10px 20px;
  background: #ecf0f1;
  color: #7f8c8d;
  border-radius: 4px;
}
</style>
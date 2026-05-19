import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import StudentHome from '../views/StudentHome.vue'
import AdminHome from '../views/AdminHome.vue'
import EquipmentList from '../views/EquipmentList.vue'
import MyBookings from '../views/MyBookings.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  {
    path: '/student',
    component: StudentHome,
    meta: { requiresAuth: true, role: 0 },
    children: [
      { path: '', redirect: '/student/equipment' },
      { path: 'equipment', component: EquipmentList },
      { path: 'my-bookings', component: MyBookings }
    ]
  },
  {
    path: '/admin',
    component: AdminHome,
    meta: { requiresAuth: true, role: 1 },
    children: [
      { path: '', redirect: '/admin/approvals' },
      { path: 'approvals', component: { template: '<div>审批列表页面（待开发）</div>' } },
      { path: 'equipment', component: { template: '<div>设备管理页面（待开发）</div>' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态和角色权限
router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 登录页直接放行
  if (to.path === '/login') {
    return true
  }

  // 未登录：跳转到登录页
  if (!token) {
    return '/login'
  }

  // 检查角色权限（防止学生访问管理员页面）
  if (to.meta.role !== undefined && Number(to.meta.role) !== Number(role)) {
    // 角色不匹配，跳转到对应角色首页
    return Number(role) === 0 ? '/student' : '/admin'
  }

  return true
})

export default router
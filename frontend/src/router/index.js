import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import StudentHome from '../views/StudentHome.vue'
import AdminHome from '../views/AdminHome.vue'
import EquipmentList from '../views/EquipmentList.vue'
import MyBookings from '../views/MyBookings.vue'

const routes = [
  // 登录页
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },

  // 学生端（角色 0）
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

  // 管理员端（角色 1）
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
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 登录页直接放行
  if (to.path === '/login') {
    next()
    return
  }

  // 未登录：跳转到登录页
  if (!token) {
    next('/login')
    return
  }

  // 检查角色权限（防止学生访问管理员页面）
  if (to.meta.role !== undefined && Number(to.meta.role) !== Number(role)) {
    // 角色不匹配，跳转到对应角色首页
    if (Number(role) === 0) {
      next('/student')
    } else {
      next('/admin')
    }
    return
  }

  next()
})

export default router
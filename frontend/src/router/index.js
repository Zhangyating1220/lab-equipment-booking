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

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.path === '/login') {
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  if (to.meta.role !== undefined && Number(to.meta.role) !== Number(role)) {
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
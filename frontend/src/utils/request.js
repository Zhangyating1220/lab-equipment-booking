import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

request.interceptors.response.use(
  response => {
    const data = response.data
    // 如果后端返回的是 Result 对象，提取 data 字段
    if (data && typeof data === 'object' && 'data' in data) {
      return data.data
    }
    return data
  },
  error => {
    if (error.response) {
      ElMessage.error(error.response.data?.message || '请求失败')
    } else if (error.request) {
      ElMessage.error('网络连接失败')
    } else {
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

export default request
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
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
  response => response.data,
  error => {
    if (error.response) {
      const { status, data } = error.response
      let message = '请求失败'
      
      // 根据状态码显示不同的错误消息
      if (status === 401) {
        message = data?.message || '未登录，请先登录'
      } else if (status === 403) {
        message = data?.message || '无权限访问'
      } else if (status === 404) {
        message = data?.message || '资源不存在'
      } else if (status >= 500) {
        message = data?.message || '系统繁忙，请稍后重试'
      } else {
        message = data?.message || '请求失败'
      }
      
      ElMessage.error(message)
    } else if (error.request) {
      ElMessage.error('网络连接失败')
    } else {
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

export default request
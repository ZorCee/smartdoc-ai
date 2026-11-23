import axios from 'axios'
import { ElMessage } from 'element-plus'

// 1. 创建 Axios 实例
const request = axios.create({
    timeout: 15000 // 请求超时时间 15 秒
})

// 2. 请求拦截器：每次发请求前，自动在请求头带上 satoken 房卡
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('satoken')
        if (token) {
            config.headers['satoken'] = token
        }
        return config
    },
    error => Promise.reject(error)
)

// 3. 响应拦截器：统一判断后端的 Result.code 状态码
request.interceptors.response.use(
    response => {
        const res = response.data
        // 如果后端返回的 code 是 200，说明业务成功，直接放行
        if (res.code === 200) {
            return res
        }
        // 如果是 401 未登录，清理本地过期房卡并跳回登录页
        if (res.code === 401) {
            ElMessage.error('登录已过期，请重新登录！')
            localStorage.removeItem('satoken')
            window.location.href = '/login'
            return Promise.reject(new Error(res.message))
        }
        // 其他业务报错（如 500 密码错误、知识库不存在），自动弹出右上角红色提示框
        ElMessage.error(res.message || '系统异常')
        return Promise.reject(new Error(res.message))
    },
    error => {
        ElMessage.error('网络连接异常，请检查后端服务是否启动')
        return Promise.reject(error)
    }
)

export default request
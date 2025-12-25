import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 30000
})

request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = token
        }
        return config
    },
    error => Promise.reject(error)
)

request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.message || 'Error')
            if (res.code === 401) {
                localStorage.removeItem('token')
                window.location.href = '/login'
            }
            return Promise.reject(new Error(res.message))
        }
        return res.data
    },
    error => {
        ElMessage.error(error.message || 'Network Error')
        return Promise.reject(error)
    }
)

export default request

import axios from 'axios'

const instance = axios.create({
  baseURL: 'http://localhost:8080', // Spring Boot 后端接口地址
  timeout: 5000
})

instance.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
})

export default instance

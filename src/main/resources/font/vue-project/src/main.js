import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import { ElMessageBox } from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import axios from 'axios'

import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/userStore'

const style = document.createElement('style');
style.textContent = `
  body, html {
    margin: 0 !important;
    padding: 0 !important;
    overflow: hidden !important;
  }
  #app {
    margin: 0 !important;
    padding: 0 !important;
    left: 0 !important;
    top: 0 !important;
    width: 100vw !important;
    height: 100vh !important;
    position: absolute !important;
  }
`;
document.head.appendChild(style);

const app = createApp(App)
const pinia = createPinia()

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 在应用挂载前初始化用户状态
const userStore = useUserStore()
if (userStore.token) {
  userStore.getUserInfo()
}

// 全局设置axios拦截器，自动添加token
// 请求拦截器
axios.interceptors.request.use(config => {
  // 从localStorage获取token
  const token = localStorage.getItem('token')
  
  // 如果有token则添加到请求头
  if (token) {
    config.headers.token = token
    console.log('请求添加token:', token)
  }
  
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器处理未登录情况
axios.interceptors.response.use(response => {
  // 如果后端返回未登录错误码
  if (response.data && (response.data.code === 401 || response.data.message === '未登录' || response.data.message === '未登录或登陆已过期，请重新登录！')) {
    console.log('检测到未登录或登录过期状态')
    // 清除token
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    
    // 显示登录对话框
    ElMessageBox.alert('登录已过期，请重新登录', '提示', {
      confirmButtonText: '去登录',
      callback: () => {
        // 使用router跳转到登录页面
        router.push('/login')
      }
    })
    
    // 清除当前用户状态
    const userStore = useUserStore()
    userStore.logout()
  }
  return response
}, error => {
  // 处理401等授权错误
  if (error.response && (error.response.status === 401 || error.response.status === 403)) {
    console.log('API请求返回未授权错误')
    // 清除token
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    
    // 显示登录对话框
    ElMessageBox.alert('登录已过期，请重新登录', '提示', {
      confirmButtonText: '去登录',
      callback: () => {
        // 使用router跳转到登录页面
        router.push('/login')
      }
    })
    
    // 清除当前用户状态
    const userStore = useUserStore()
    userStore.logout()
  }
  return Promise.reject(error)
})

app.mount('#app')

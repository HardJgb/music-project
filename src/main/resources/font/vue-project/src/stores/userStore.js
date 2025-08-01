import { defineStore } from 'pinia'
import axios from 'axios'
import { API_BASE_URL } from '@/api/config'

// 用户状态管理
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    loading: false,
    error: null
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token && !!state.userInfo,
    username: (state) => state.userInfo?.userName || '未登录'
  },
  
  actions: {
    // 登录方法
    async login(userName, userPwd) {
      this.loading = true
      this.error = null
      
      try {
        // 调用登录接口
        const response = await axios.post(`${API_BASE_URL}/user/login`, {
          userName,
          userPwd
        })
        
        console.log('登录响应:', response.data)
        
        // 检查登录是否成功
        if (response.data.code !== 200) {
          this.error = response.data.message || '登录失败'
          return false
        }
        
        // 查看响应数据结构，找出token的确切位置
        console.log('登录响应的完整数据:', JSON.stringify(response.data))
        
        // 直接从响应中提取token (检查所有可能的位置)
        let token = null
        
        if (response.data.token) {
          token = response.data.token
        } else if (response.data.data && response.data.data.token) {
          token = response.data.data.token
        } else if (response.data.data && typeof response.data.data === 'string') {
          // 有些后端可能直接返回token字符串作为data
          token = response.data.data
        }
        
        console.log('提取到的token:', token)
        
        if (token) {
          this.token = token
          localStorage.setItem('token', token)
          console.log('Token已存储到localStorage:', token)
        } else {
          console.error('未能从响应中提取token')
          // 创建一个调试对象，帮助发现token位置
          const debugObj = {
            hasToken: !!response.data.token,
            hasDataToken: !!(response.data.data && response.data.data.token),
            dataType: response.data.data ? typeof response.data.data : 'null',
            responseKeys: Object.keys(response.data)
          }
          console.log('响应调试信息:', debugObj)
        }
        
        // 处理用户信息...
        if (response.data.data) {
          const userId = response.data.data.uid || response.data.data.id || 0;
          this.userInfo = {
            uid: userId,
            userName: userName
          }
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          // 单独保存userId以便其他组件使用
          localStorage.setItem('userId', userId)
          return !!token // 只有当token存在时才返回true
        } else {
          this.userInfo = { userName }
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          return !!token
        }
      } catch (err) {
        console.error('登录过程出错:', err)
        this.error = '网络错误，请稍后再试'
        return false
      } finally {
        this.loading = false
      }
    },
    
    // 简化getUserInfo方法，如果后期需要使用
    async getUserInfo() {
      // 由于后端JWT解析问题，暂时不调用此接口
      return false
    },
    
    // 注册方法
    async register(userName, userPwd) {
      this.loading = true
      this.error = null
      
      try {
        const response = await axios.post(`${API_BASE_URL}/user/register`, {
          userName,
          userPwd
        })
        
        if (response.data.code === 200) {
          return true
        } else {
          this.error = response.data.message || '注册失败'
          return false
        }
      } catch (err) {
        console.error('注册错误:', err)
        this.error = '网络错误，请稍后再试'
        return false
      } finally {
        this.loading = false
      }
    },
    
    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userId')
    },
    
    // 添加更新用户信息方法
    async updateUserInfo(userData) {
      try {
        // 如果有后端API可以调用
        // const response = await axios.post(`${API_BASE_URL}/user/updateInfo`, userData)
        
        // 暂时直接更新本地数据
        this.userInfo = { ...userData }
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        return true
      } catch (err) {
        console.error('更新用户信息错误:', err)
        return false
      }
    }
  }
}) 
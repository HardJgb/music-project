<template>
  <div class="app-header">
    <div class="header-left">
      <div class="nav-buttons">
        <!-- <el-button :icon="ArrowLeft" circle class="nav-btn" />
        <el-button :icon="ArrowRight" circle class="nav-btn" /> -->
      </div>
    </div>
    <!-- <div class="header-center">
      <div class="user-info">

        <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
      </div>
    </div> -->
    <div class="header-right">
      <!-- 登录状态显示 -->
      <div v-if="userStore.userInfo" class="user-info" @click="showUserMenu = !showUserMenu">
        <span class="username">{{ userStore.userInfo.userName }}</span>
        <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
        
        <!-- 用户菜单 -->
        <div v-if="showUserMenu" class="user-menu">
          <div class="menu-item" @click="goToUserProfile">个人中心</div>
          <div class="menu-item" @click="logout">退出登录</div>
        </div>
      </div>
      
      <!-- 未登录状态显示登录按钮 -->
      <el-button 
        v-else 
        type="primary" 
        class="login-button" 
        @click="showLoginDialog"
      >
        登录
      </el-button>
      
      <el-button :icon="Setting" circle class="theme-btn" @click="toggleTheme" />
      <el-button :icon="Message" circle class="message-btn" />
    </div>
    
    <!-- 登录/注册对话框 -->
    <login-dialog 
      v-model:visible="loginDialogVisible"
      @login-success="handleLoginSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight, Setting, Message, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import LoginDialog from '@/components/auth/LoginDialog.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const theme = ref('light')
const loginDialogVisible = ref(false)
const showUserMenu = ref(false)
const userStore = useUserStore()

// 计算属性：显示用户名
const displayUsername = computed(() => {
  if (!userStore.userInfo) return '用户';
  return userStore.userInfo.userName || userStore.userInfo.username || '用户';
})

// 切换主题
const toggleTheme = () => {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
  // 这里可以添加切换主题的逻辑
}

// 显示登录对话框
const showLoginDialog = () => {
  loginDialogVisible.value = true
}

// 处理登录成功
const handleLoginSuccess = () => {
  // 登录成功后，用户信息已经在store中了
  // 这里不需要做额外处理
}

// 退出登录
const logout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  showUserMenu.value = false
}

// 跳转到用户个人中心
const goToUserProfile = () => {
  router.push('/user/profile')
  showUserMenu.value = false
}

// 组件挂载时初始化
onMounted(async () => {
  // 如果有token但没有用户信息，尝试获取用户信息
  if (userStore.token && !userStore.userInfo) {
    await userStore.getUserInfo()
  }
})
</script>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 20px;
  background-color: #ec4141;
  color: white;
}

.header-left, .header-right, .header-center {
  display: flex;
  align-items: center;
}

.header-center {
  flex: 1;
  justify-content: center;
}

.nav-buttons {
  display: flex;
  gap: 10px;
}

.nav-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  position: relative;
  padding: 5px 10px;
  border-radius: 4px;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.username {
  font-size: 14px;
  color: white;
}

.dropdown-icon {
  font-size: 12px;
  margin-top: 2px;
}

.user-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 5px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 100;
  width: 120px;
}

.menu-item {
  padding: 10px 15px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.theme-btn, .message-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  margin-left: 10px;
}

.right-section {
  display: flex;
  align-items: center;
  margin-left: auto; /* 确保右对齐 */
  gap: 10px;
}

.login-button {
  background-color: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 4px;
}

.login-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.setting-button {
  background-color: transparent;
  border: none;
  color: white;
}

.setting-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
}
</style> 
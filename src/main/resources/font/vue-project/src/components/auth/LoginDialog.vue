<template>
  <el-dialog
    v-model="dialogVisible"
    :title="false"
    width="420px"
    destroy-on-close
    @closed="resetForm"
    class="login-dialog"
  >
    <div class="dialog-header">
      <h2 class="dialog-title">{{ isLogin ? '登录' : '注册' }}</h2>
      <p class="dialog-subtitle">{{ isLogin ? '欢迎回来' : '创建新账号' }}</p>
    </div>
    
    <!-- 表单区域 -->
    <el-form 
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      size="large"
      class="login-form"
    >
      <!-- 用户名 -->
      <el-form-item label="用户名" prop="userName">
        <el-input 
          v-model="form.userName" 
          placeholder="请输入用户名"
          :prefix-icon="User"
        />
      </el-form-item>
      
      <!-- 密码 -->
      <el-form-item label="密码" prop="userPwd">
        <el-input 
          v-model="form.userPwd" 
          type="password"
          placeholder="请输入密码"
          show-password
          :prefix-icon="Lock"
        />
      </el-form-item>
      
      <!-- 注册时显示确认密码 -->
      <el-form-item v-if="!isLogin" label="确认密码" prop="confirmPwd">
        <el-input 
          v-model="form.confirmPwd" 
          type="password"
          placeholder="请再次输入密码"
          show-password
          :prefix-icon="Lock"
        />
      </el-form-item>
      
      <!-- 错误信息提示 -->
      <div v-if="userStore.error" class="error-message">
        {{ userStore.error }}
      </div>
      
      <!-- 登录/注册按钮 -->
      <el-form-item>
        <el-button 
          type="primary" 
          :loading="userStore.loading" 
          @click="submitForm"
          class="submit-button"
        >
          {{ isLogin ? '登录' : '注册' }}
        </el-button>
      </el-form-item>
      
      <!-- 切换登录/注册 -->
      <div class="switch-mode-container">
        <span class="switch-mode" @click="toggleMode">
          {{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}
        </span>
      </div>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'login-success'])

// 双向绑定对话框显示状态
const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 表单和验证规则
const formRef = ref(null)
const isLogin = ref(true)
const userStore = useUserStore()

const form = ref({
  userName: '',
  userPwd: '',
  confirmPwd: ''
})

// 验证密码是否一致
const validatePass2 = (rule, value, callback) => {
  if (!isLogin.value) {
    if (value === '') {
      callback(new Error('请再次输入密码'))
    } else if (value !== form.value.userPwd) {
      callback(new Error('两次输入密码不一致'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

const rules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  userPwd: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPwd: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

// 切换登录/注册模式
const toggleMode = () => {
  isLogin.value = !isLogin.value
  resetForm()
}

// 重置表单
const resetForm = () => {
  form.value = {
    userName: '',
    userPwd: '',
    confirmPwd: ''
  }
  formRef.value?.resetFields()
  userStore.error = null
}

// 提交表单
const submitForm = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      if (isLogin.value) {
        // 登录逻辑
        const success = await userStore.login(form.value.userName, form.value.userPwd)
        if (success) {
          // 关闭对话框
          dialogVisible.value = false
          // 通知外部组件登录成功
          emit('login-success')
          // 显示欢迎消息，使用登录表单中的用户名
          ElMessage.success(`登录成功，欢迎 ${form.value.userName}`)
        }
      } else {
        // 注册逻辑
        const success = await userStore.register(form.value.userName, form.value.userPwd)
        if (success) {
          isLogin.value = true
          ElMessage.success('注册成功，请登录')
        }
      }
    }
  })
}
</script>

<style scoped>
.login-dialog :deep(.el-dialog__header) {
  display: none;
}

.login-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.dialog-header {
  text-align: center;
  margin-bottom: 30px;
}

.dialog-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 10px;
}

.dialog-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  width: 100%;
}

.error-message {
  color: #F56C6C;
  font-size: 14px;
  margin: 10px 0;
  text-align: center;
  padding: 8px;
  background-color: #FEF0F0;
  border-radius: 4px;
}

.submit-button {
  width: 100%;
  margin-top: 10px;
  height: 40px;
  font-size: 16px;
  background-color: #ec4141;
  border-color: #ec4141;
  transition: all 0.3s;
}

.submit-button:hover {
  background-color: #d73535;
  border-color: #d73535;
}

.switch-mode-container {
  margin-top: 20px;
  text-align: center;
}

.switch-mode {
  color: #ec4141;
  cursor: pointer;
  font-size: 14px;
  transition: color 0.3s;
}

.switch-mode:hover {
  color: #d73535;
  text-decoration: underline;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 4px;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #ec4141 inset;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 500;
}

/* 添加表单项动画 */
.el-form-item {
  transition: all 0.3s;
}

.el-form-item:hover {
  transform: translateY(-2px);
}
</style> 
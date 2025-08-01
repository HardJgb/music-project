<template>
  <div class="user-profile">
    <div class="profile-header">
      <div class="profile-cover"></div>
      <div class="user-avatar" @click="triggerFileUpload">
        <el-avatar :size="80" :src="avatarUrl">{{ userInitial }}</el-avatar>
        <div class="avatar-upload-overlay">
          <el-icon><UploadFilled /></el-icon>
          <span>上传头像</span>
        </div>
        <input
          type="file"
          ref="fileInput"
          style="display: none"
          accept="image/*"
          @change="handleFileChange"
        />
      </div>
      <h1 class="user-name">{{ userStore.userInfo?.userName || '用户' }}</h1>
    </div>

    <div class="profile-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人信息" name="info">
          <div class="profile-info">
            <div class="info-card">
              <h3 class="card-title">基本信息</h3>
              <el-form 
                label-position="top" 
                :model="userForm"
                :disabled="!isEditing"
              >
                <el-form-item label="用户名">
                  <el-input v-model="userForm.userName" />
                </el-form-item>
                
                <el-form-item label="个人简介">
                  <el-input 
                    v-model="userForm.bio" 
                    type="textarea" 
                    rows="3" 
                    placeholder="介绍一下自己吧..."
                  />
                </el-form-item>
                
                <el-form-item v-if="isEditing">
                  <div class="form-buttons">
                    <el-button @click="cancelEdit">取消</el-button>
                    <el-button type="primary" @click="saveUserInfo">保存</el-button>
                  </div>
                </el-form-item>
              </el-form>
              
              <el-button 
                v-if="!isEditing" 
                type="primary" 
                plain 
                @click="startEdit"
                class="edit-button"
              >
                编辑信息
              </el-button>
            </div>
            
            <div class="info-card">
              <h3 class="card-title">账号安全</h3>
              <div class="security-item">
                <div class="security-info">
                  <span class="security-label">修改密码</span>
                  <span class="security-desc">定期修改密码可以保护账号安全</span>
                </div>
                <el-button @click="showChangePasswordDialog = true">修改</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="我的收藏" name="favorites">
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          
          <div v-else-if="userFavorites.length === 0" class="empty-favorites">
            <el-empty description="暂无收藏内容" />
            <el-button type="primary" @click="goToExplore">去发现音乐</el-button>
          </div>
          
          <div v-else class="favorites-list">
            <div 
              v-for="(song, index) in userFavorites" 
              :key="song.id"
              class="favorite-item"
            >
              <div class="favorite-index">{{ index + 1 }}</div>
              <div class="favorite-cover">
                <img :src="song.coverUrl || 'default-cover.jpg'" alt="封面">
              </div>
              <div class="favorite-info">
                <div class="favorite-name">{{ song.songName }}</div>
                <div class="favorite-artist">{{ song.artist }}</div>
              </div>
              <div class="favorite-actions">
                <el-icon @click="playSong(song)"><VideoPlay /></el-icon>
                <el-icon @click="removeFavorite(song)"><Delete /></el-icon>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="最近播放" name="recent">
          <el-empty description="暂无最近播放记录" />
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showChangePasswordDialog"
      title="修改密码"
      width="400px"
    >
      <el-form 
        :model="passwordForm" 
        :rules="passwordRules"
        ref="passwordFormRef"
        label-position="top"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showChangePasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="changePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { useRouter } from 'vue-router'
import { usePlayerStore } from '@/stores/playerStore'
import { ElMessage } from 'element-plus'
import { VideoPlay, Delete, UploadFilled } from '@element-plus/icons-vue'
import { API_BASE_URL } from '@/api/config'
import axios from 'axios'

const userStore = useUserStore()
const router = useRouter()
const playerStore = usePlayerStore()
const activeTab = ref('info')
const isEditing = ref(false)
const showChangePasswordDialog = ref(false)
const userFavorites = ref([])
const loading = ref(false)
const fileInput = ref(null)

// 用户头像和首字母
const avatarUrl = computed(() => {
  console.log('User avatar path:', userStore.userInfo?.avatar);
  if (userStore.userInfo?.avatar) {
    // 检查头像路径是否已经包含完整URL
    if (userStore.userInfo.avatar.startsWith('http')) {
      console.log('使用完整头像URL:', userStore.userInfo.avatar);
      return userStore.userInfo.avatar;
    }
    // 确保路径正确（移除可能重复的斜杠）
    const avatarPath = userStore.userInfo.avatar.startsWith('/') 
      ? userStore.userInfo.avatar 
      : '/' + userStore.userInfo.avatar;
    console.log('拼接后的头像URL:', `${API_BASE_URL}${avatarPath}`);
    return `${API_BASE_URL}${avatarPath}`;
  }
  // 使用默认头像
  console.log('使用默认头像:', `${API_BASE_URL}/public/image/default-avatar.jpg`);
  return `${API_BASE_URL}/public/image/default-avatar.jpg`;
})

const userInitial = computed(() => {
  const userName = userStore.userInfo?.userName || '用户'
  return userName.charAt(0).toUpperCase()
})

// 用户表单数据
const userForm = reactive({
  userName: userStore.userInfo?.userName || '',
  bio: userStore.userInfo?.bio || ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (passwordForm.confirmPassword !== '') {
      passwordFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

const passwordFormRef = ref(null)

// 方法
const startEdit = () => {
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  // 重置表单数据
  userForm.userName = userStore.userInfo?.userName || ''
  userForm.bio = userStore.userInfo?.bio || ''
}

const saveUserInfo = () => {
  // 这里实现保存逻辑
  ElMessage.success('个人信息已更新')
  isEditing.value = false
  
  // 更新Store中的用户信息
  userStore.updateUserInfo({
    ...userStore.userInfo,
    userName: userForm.userName,
    bio: userForm.bio
  })
}

const changePassword = () => {
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      // 这里实现修改密码逻辑
      ElMessage.success('密码已修改')
      showChangePasswordDialog.value = false
      
      // 重置表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  })
}

// 获取用户收藏列表
const fetchUserFavorites = async () => {
  if (!userStore.isLoggedIn) return
  
  loading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/favorites`, {
      headers: { token: userStore.token }
    })
    
    if (response.data.code === 200) {
      userFavorites.value = response.data.data
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 播放收藏的歌曲
const playSong = (song) => {
  playerStore.playSong({
    id: song.songId,
    name: song.songName,
    artist: song.artist,
    coverUrl: song.coverUrl,
    audioUrl: `${API_BASE_URL}/audio/${song.songId}`
  })
}

// 移除收藏
const removeFavorite = async (song) => {
  try {
    await axios.delete(`${API_BASE_URL}/favorites/${song.songId}`, {
      headers: { token: userStore.token }
    })
    
    // 从列表中移除
    userFavorites.value = userFavorites.value.filter(item => item.songId !== song.songId)
    ElMessage.success('已取消收藏')
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 跳转到首页发现音乐
const goToExplore = () => {
  router.push('/')
}

// 触发文件上传
const triggerFileUpload = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  fileInput.value.click()
}

// 处理文件选择
const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 检查文件类型
  if (!file.type.includes('image')) {
    ElMessage.error('请上传图片文件')
    return
  }

  // 检查文件大小，限制为2MB
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  // 创建FormData对象
  const formData = new FormData()
  formData.append('file', file)

  try {
    loading.value = true
    console.log('开始上传头像...');
    
    // 注意：确保Content-Type设置正确，且不要自动添加其他头信息
    const response = await axios.post(`${API_BASE_URL}/user/avatar`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'token': userStore.token
      },
      // 防止axios自动处理Content-Type
      transformRequest: [(data) => {
        return data;
      }]
    })

    console.log('头像上传响应:', response.data);
    if (response.data.code === 200) {
      // 更新用户信息中的头像
      userStore.updateUserInfo({
        ...userStore.userInfo,
        avatar: response.data.data
      })
      console.log('更新后的头像路径:', response.data.data);
      ElMessage.success('头像上传成功')
      
      // 强制刷新用户信息以获取最新头像
      await fetchUserInfo();
      
      // 强制刷新头像（随机参数防止缓存）
      if (userStore.userInfo?.avatar && userStore.userInfo.avatar.startsWith('http')) {
        const randomParam = `?t=${new Date().getTime()}`;
        userStore.updateUserInfo({
          ...userStore.userInfo,
          avatar: userStore.userInfo.avatar.split('?')[0] + randomParam
        });
      }
    } else {
      ElMessage.error(response.data.message || '上传失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('上传失败，请稍后再试')
  } finally {
    loading.value = false
    // 重置文件输入框
    event.target.value = ''
  }
}

// 获取用户信息
const fetchUserInfo = async () => {
  if (!userStore.isLoggedIn) return;
  
  try {
    const response = await axios.get(`${API_BASE_URL}/user/info`, {
      headers: { token: userStore.token }
    });
    
    if (response.data.code === 200 && response.data.data) {
      console.log('获取到用户信息:', response.data.data);
      userStore.updateUserInfo(response.data.data);
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
  }
}

onMounted(async () => {
  // 初始化用户数据
  if (userStore.isLoggedIn) {
    await fetchUserInfo();
  }
  fetchUserFavorites();
})
</script>

<style scoped>
.user-profile {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.profile-header {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 30px;
  margin-bottom: 30px;
  border-bottom: 1px solid #f0f0f0;
}

.profile-cover {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 150px;
  background: linear-gradient(to right, #ec4141, #ff7171);
  border-radius: 8px;
}

.user-avatar {
  position: relative;
  margin-top: 100px;
  z-index: 1;
  border: 4px solid white;
  border-radius: 50%;
  background-color: white;
  cursor: pointer;
  overflow: hidden;
}

.user-name {
  margin-top: 15px;
  font-size: 24px;
  color: #303133;
}

.profile-tabs {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.profile-info {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

@media (min-width: 768px) {
  .profile-info {
    grid-template-columns: 2fr 1fr;
  }
}

.info-card {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  position: relative;
}

.card-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #303133;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.security-item:last-child {
  border-bottom: none;
}

.security-info {
  display: flex;
  flex-direction: column;
}

.security-label {
  font-size: 16px;
  color: #303133;
  margin-bottom: 5px;
}

.security-desc {
  font-size: 13px;
  color: #909399;
}

.form-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.edit-button {
  position: absolute;
  top: 20px;
  right: 20px;
}

/* 添加收藏列表样式 */
.favorites-list {
  margin-top: 20px;
}

.favorite-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #eee;
  transition: all 0.3s;
}

.favorite-item:hover {
  background-color: #f9f9f9;
}

.favorite-index {
  width: 30px;
  color: #999;
  text-align: center;
}

.favorite-cover {
  width: 50px;
  height: 50px;
  margin-right: 15px;
  overflow: hidden;
  border-radius: 4px;
}

.favorite-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.favorite-info {
  flex: 1;
}

.favorite-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.favorite-artist {
  font-size: 12px;
  color: #999;
}

.favorite-actions {
  display: flex;
  gap: 12px;
}

.favorite-actions .el-icon {
  cursor: pointer;
  font-size: 18px;
  color: #909399;
  transition: all 0.3s;
}

.favorite-actions .el-icon:hover {
  color: #ec4141;
  transform: scale(1.2);
}

.empty-favorites {
  text-align: center;
  margin-top: 40px;
}

.empty-favorites .el-button {
  margin-top: 20px;
}

.loading-container {
  padding: 20px;
}

.avatar-upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.user-avatar:hover .avatar-upload-overlay {
  opacity: 1;
}

.avatar-upload-overlay .el-icon {
  font-size: 24px;
  margin-bottom: 5px;
}
</style> 
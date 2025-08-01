<template>
  <el-dialog
    v-model="dialogVisible"
    title="创建歌单"
    width="500px"
    @closed="resetForm"
  >
    <el-form 
      :model="playlistForm" 
      :rules="rules" 
      ref="playlistFormRef" 
      label-width="80px"
      status-icon
    >
      <el-form-item label="歌单名称" prop="name">
        <el-input 
          v-model="playlistForm.name" 
          placeholder="请输入歌单名称" 
          show-word-limit 
          maxlength="50"
        />
      </el-form-item>
      
      <el-form-item label="歌单封面">
        <div class="cover-upload">
          <div class="cover-preview" @click="triggerFileInput">
            <img v-if="imagePreview" :src="imagePreview" alt="歌单封面" />
            <el-icon v-else><Plus /></el-icon>
          </div>
          <el-button type="primary" size="small" @click="triggerFileInput">上传封面</el-button>
          <input 
            type="file" 
            ref="fileInput" 
            style="display: none" 
            accept="image/*" 
            @change="handleFileChange"
          />
        </div>
      </el-form-item>
      
      <el-form-item label="歌单描述">
        <el-input 
          v-model="playlistForm.description" 
          type="textarea" 
          placeholder="请输入歌单描述" 
          show-word-limit 
          maxlength="200"
          rows="4"
        />
      </el-form-item>
      
      <el-form-item>
        <el-checkbox v-model="playlistForm.isPrivate">设置为私密歌单</el-checkbox>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="loading">创建</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createUserPlaylist } from '@/api/userPlaylist'

const dialogVisible = ref(false)
const loading = ref(false)
const imagePreview = ref('')
const fileInput = ref(null)
const playlistFormRef = ref(null)

// 表单数据
const playlistForm = reactive({
  name: '',
  description: '',
  isPrivate: false,
  coverImage: null
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入歌单名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value.click()
}

// 处理文件选择
const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return

  // 验证文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return
  }

  // 验证文件大小 (2MB)
  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  // 设置预览
  playlistForm.coverImage = file
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

// 重置表单
const resetForm = () => {
  if (playlistFormRef.value) {
    playlistFormRef.value.resetFields()
  }
  imagePreview.value = ''
  playlistForm.coverImage = null
}

// 提交表单
const submitForm = async () => {
  if (!playlistFormRef.value) return
  
  await playlistFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await createUserPlaylist(playlistForm)
        if (response.data.code === 200) {
          ElMessage.success('创建成功')
          dialogVisible.value = false
          // 触发创建成功事件
          emit('created', response.data.data)
        } else {
          ElMessage.error(response.data.msg || '创建失败')
        }
      } catch (error) {
        console.error('创建歌单失败:', error)
        ElMessage.error('创建失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }
  })
}

// 显示对话框
const show = () => {
  dialogVisible.value = true
}

// 定义暴露的方法
defineExpose({
  show
})

// 定义事件
const emit = defineEmits(['created'])
</script>

<style scoped>
.cover-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.cover-preview {
  width: 120px;
  height: 120px;
  border: 1px dashed #ccc;
  border-radius: 4px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  overflow: hidden;
  background-color: #f7f7f7;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-preview .el-icon {
  font-size: 30px;
  color: #999;
}
</style> 
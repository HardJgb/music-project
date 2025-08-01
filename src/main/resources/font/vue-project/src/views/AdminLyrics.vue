<template>
  <div class="admin-lyrics-container">
    <h1 class="page-title">歌词管理</h1>
    
    <el-card class="search-card">
      <div class="search-area">
        <el-input
          v-model="searchQuery"
          placeholder="搜索歌曲或歌手名称"
          class="search-input"
          clearable
          @keyup.enter="searchSongs"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="searchSongs">搜索</el-button>
      </div>
    </el-card>
    
    <el-card class="song-list-card">
      <div class="song-list-header">
        <h2 class="section-title">歌曲列表</h2>
        <div class="song-count">共 {{ songs.length }} 首歌曲</div>
      </div>
      
      <el-table
        :data="songs"
        style="width: 100%"
        @row-click="selectSong"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="封面" width="80">
          <template #default="scope">
            <el-image
              :src="getProcessedImageUrl(scope.row.coverImg)"
              :alt="scope.row.name"
              style="width: 60px; height: 60px; border-radius: 4px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="歌曲名称" />
        <el-table-column prop="artist" label="歌手" />
        <el-table-column label="歌词状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.hasLyrics" type="success">已有歌词</el-tag>
            <el-tag v-else type="danger">无歌词</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click.stop="editLyrics(scope.row)">
              {{ scope.row.hasLyrics ? '编辑歌词' : '添加歌词' }}
            </el-button>
            <el-button 
              v-if="scope.row.hasLyrics" 
              size="small" 
              type="danger" 
              @click.stop="confirmDeleteLyrics(scope.row.id)"
            >
              删除歌词
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 歌词编辑对话框 -->
    <el-dialog
      v-model="lyricsDialogVisible"
      :title="currentSong.id ? (currentSong.hasLyrics ? '编辑歌词' : '添加歌词') : '歌词管理'"
      width="60%"
      destroy-on-close
    >
      <div v-if="currentSong.id" class="lyrics-dialog-content">
        <div class="song-info">
          <el-image
            :src="getProcessedImageUrl(currentSong.coverImg)"
            :alt="currentSong.name"
            style="width: 80px; height: 80px; border-radius: 4px"
            fit="cover"
          />
          <div class="song-details">
            <h3>{{ currentSong.name }}</h3>
            <p>歌手: {{ currentSong.artist }}</p>
          </div>
        </div>
        
        <el-form :model="lyricsForm" label-width="80px">
          <el-form-item label="歌词格式">
            <el-radio-group v-model="lyricsForm.format">
              <el-radio label="LRC">LRC格式</el-radio>
              <el-radio label="PLAIN">纯文本</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="歌词来源">
            <el-input v-model="lyricsForm.source" placeholder="例如: 网络收集, 用户上传等"></el-input>
          </el-form-item>
          
          <el-form-item label="歌词内容">
            <el-input
              v-model="lyricsForm.content"
              type="textarea"
              :rows="15"
              placeholder="请输入歌词内容，LRC格式请包含时间标签 [mm:ss.xx]"
            ></el-input>
          </el-form-item>
        </el-form>
        
        <div class="lyrics-preview" v-if="lyricsForm.content && lyricsForm.format === 'LRC'">
          <h4>歌词预览</h4>
          <div class="preview-content">
            <p v-for="(line, index) in parsedLyrics" :key="index" class="lyric-line">
              {{ line.text }}
            </p>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="lyricsDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveLyrics">保存歌词</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { API_BASE_URL } from '@/api/config'

// 搜索相关
const searchQuery = ref('')
const songs = ref([])

// 选中的歌曲和歌词表单
const currentSong = ref({})
const lyricsForm = ref({
  songId: null,
  content: '',
  format: 'LRC',
  source: ''
})

// 对话框控制
const lyricsDialogVisible = ref(false)

// 解析后的歌词预览
const parsedLyrics = computed(() => {
  if (!lyricsForm.value.content || lyricsForm.value.format !== 'LRC') {
    return []
  }
  
  // 解析LRC格式歌词
  const result = []
  const lines = lyricsForm.value.content.split('\n')
  
  lines.forEach(line => {
    line = line.trim()
    if (!line) return
    
    // 匹配时间标签 [mm:ss.xx]
    const timeTagRegex = /\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)/
    
    // 匹配歌曲信息标签 [ti:标题]
    const infoTagRegex = /\[(\w+):([^\]]*)\]/
    
    if (timeTagRegex.test(line)) {
      // 解析带时间的歌词行
      const match = line.match(timeTagRegex)
      if (match) {
        const text = match[4].trim()
        result.push({ text })
      }
    } else if (infoTagRegex.test(line)) {
      // 解析歌曲信息标签
      const match = line.match(infoTagRegex)
      if (match) {
        const tag = match[1].toLowerCase()
        const value = match[2]
        if (tag && value) {
          result.push({ text: `[${tag}:${value}]` })
        }
      }
    }
  })
  
  return result
})

// 加载所有歌曲
const loadSongs = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/songs`)
    if (response.data.code === 200) {
      const songList = response.data.data || []
      
      // 检查每首歌是否有歌词
      for (const song of songList) {
        try {
          const lyricsResponse = await axios.get(`${API_BASE_URL}/api/songs/${song.id}/lyrics`)
          song.hasLyrics = lyricsResponse.data.code === 200 && lyricsResponse.data.data != null
        } catch (error) {
          song.hasLyrics = false
        }
      }
      
      songs.value = songList
    } else {
      ElMessage.error('获取歌曲列表失败')
    }
  } catch (error) {
    console.error('加载歌曲列表失败:', error)
    ElMessage.error('加载歌曲列表失败')
  }
}

// 搜索歌曲
const searchSongs = async () => {
  if (!searchQuery.value.trim()) {
    await loadSongs()
    return
  }
  
  // 从已加载的歌曲中过滤
  const query = searchQuery.value.toLowerCase()
  songs.value = songs.value.filter(song => 
    song.name.toLowerCase().includes(query) || 
    song.artist.toLowerCase().includes(query)
  )
}

// 选择歌曲
const selectSong = (row) => {
  currentSong.value = row
}

// 处理图片URL
const getProcessedImageUrl = (url) => {
  if (!url) {
    return `${API_BASE_URL}/public/image/default-cover.jpg`
  }

  if (!url.startsWith('http')) {
    if (!url.includes('/public/image/')) {
      url = '/public/image/' + url
    }
    return `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
  }
  
  return url
}

// 编辑歌词
const editLyrics = async (song) => {
  currentSong.value = song
  
  // 重置表单
  lyricsForm.value = {
    songId: song.id,
    content: '',
    format: 'LRC',
    source: ''
  }
  
  // 如果有歌词，加载现有歌词
  if (song.hasLyrics) {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/songs/${song.id}/lyrics`)
      if (response.data.code === 200 && response.data.data) {
        lyricsForm.value = {
          songId: song.id,
          content: response.data.data.content || '',
          format: response.data.data.format || 'LRC',
          source: response.data.data.source || ''
        }
      }
    } catch (error) {
      console.error('加载歌词失败:', error)
      ElMessage.error('加载歌词失败')
    }
  }
  
  lyricsDialogVisible.value = true
}

// 保存歌词
const saveLyrics = async () => {
  if (!lyricsForm.value.content.trim()) {
    ElMessage.warning('歌词内容不能为空')
    return
  }
  
  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/songs/${currentSong.value.id}/lyrics`,
      lyricsForm.value
    )
    
    if (response.data.code === 200) {
      ElMessage.success('歌词保存成功')
      lyricsDialogVisible.value = false
      
      // 更新歌曲列表中的歌词状态
      const index = songs.value.findIndex(s => s.id === currentSong.value.id)
      if (index !== -1) {
        songs.value[index].hasLyrics = true
      }
    } else {
      ElMessage.error(`保存失败: ${response.data.message}`)
    }
  } catch (error) {
    console.error('保存歌词失败:', error)
    ElMessage.error('保存歌词失败: ' + (error.response?.data?.message || error.message))
  }
}

// 确认删除歌词
const confirmDeleteLyrics = (songId) => {
  ElMessageBox.confirm(
    '确定要删除这首歌的歌词吗？此操作不可恢复。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    deleteLyrics(songId)
  }).catch(() => {
    // 用户取消删除
  })
}

// 删除歌词
const deleteLyrics = async (songId) => {
  try {
    const response = await axios.delete(`${API_BASE_URL}/api/songs/${songId}/lyrics`)
    
    if (response.data.code === 200) {
      ElMessage.success('歌词删除成功')
      
      // 更新歌曲列表中的歌词状态
      const index = songs.value.findIndex(s => s.id === songId)
      if (index !== -1) {
        songs.value[index].hasLyrics = false
      }
    } else {
      ElMessage.error(`删除失败: ${response.data.message}`)
    }
  } catch (error) {
    console.error('删除歌词失败:', error)
    ElMessage.error('删除歌词失败: ' + (error.response?.data?.message || error.message))
  }
}

// 监听搜索输入框清除事件
watch(searchQuery, (newVal) => {
  if (!newVal) {
    loadSongs()
  }
})

onMounted(() => {
  loadSongs()
})
</script>

<style scoped>
.admin-lyrics-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.search-card {
  margin-bottom: 20px;
}

.search-area {
  display: flex;
  gap: 10px;
}

.search-input {
  width: 300px;
}

.song-list-card {
  margin-bottom: 20px;
}

.song-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-title {
  font-size: 18px;
  margin: 0;
  color: #333;
}

.song-count {
  color: #888;
  font-size: 14px;
}

.lyrics-dialog-content {
  padding: 10px;
}

.song-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.song-details {
  margin-left: 15px;
}

.song-details h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.song-details p {
  margin: 0;
  color: #666;
}

.lyrics-preview {
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border: 1px solid #eee;
}

.lyrics-preview h4 {
  margin-top: 0;
  color: #333;
}

.preview-content {
  max-height: 200px;
  overflow-y: auto;
}

.lyric-line {
  margin: 5px 0;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .search-area {
    flex-direction: column;
  }
  
  .search-input {
    width: 100%;
  }
}
</style> 
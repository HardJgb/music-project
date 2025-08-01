<template>
  <div class="category-view">
    <h1 class="page-title">歌单分类</h1>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-else class="category-content">
      <!-- 分类选择 -->
      <div class="category-tabs">
        <ul class="category-list">
          <li 
            v-for="category in categories" 
            :key="category"
            :class="{ 'active': activeCategory === category }"
            @click="activeCategory = category; fetchPlaylistsByCategory(category)"
          >
            {{ category }}
          </li>
        </ul>
      </div>
      
      <div v-if="loadingPlaylists" class="loading-playlists">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="!categoryPlaylists.length" class="empty-playlist">
        <el-empty description="该分类下暂无歌单" />
      </div>
      
      <div v-else class="playlist-grid">
        <div 
          v-for="playlist in categoryPlaylists" 
          :key="playlist.id" 
          class="playlist-card"
          @click="navigateToPlaylist(playlist.id)"
        >
          <div class="playlist-cover">
            <img :src="getImageUrl(playlist.coverImg)" :alt="playlist.name">
            <div class="playlist-play-count">
              <el-icon><Headset /></el-icon>
              <span>{{ formatPlayCount(playlist.playCount || 0) }}</span>
            </div>
            <div class="playlist-overlay">
              <el-icon><VideoPlay /></el-icon>
            </div>
          </div>
          <div class="playlist-info">
            <div class="playlist-name">{{ playlist.name }}</div>
            <div class="playlist-creator">
              by {{ playlist.creator || "未知" }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { API_BASE_URL } from '@/api/config'
import { VideoPlay, Headset } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(true)
const error = ref(null)
const categories = ref([])
const activeCategory = ref('')
const categoryPlaylists = ref([])
const loadingPlaylists = ref(false)

// 获取所有歌单分类
const fetchCategories = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await axios.get(`${API_BASE_URL}/api/playlists/categories`)
    
    if (response.data.code === 200) {
      categories.value = ['全部', ...(response.data.data || [])]
      if (categories.value.length > 0) {
        activeCategory.value = categories.value[0]
        fetchPlaylistsByCategory(activeCategory.value)
      }
    } else {
      error.value = response.data.message || '获取歌单分类失败'
    }
  } catch (err) {
    console.error('获取歌单分类失败:', err)
    error.value = '网络错误，请稍后再试'
  } finally {
    loading.value = false
  }
}

// 根据分类获取歌单
const fetchPlaylistsByCategory = async (category) => {
  loadingPlaylists.value = true
  
  try {
    let response;
    
    // 如果是"全部"分类，则获取所有歌单
    if (category === '全部') {
      response = await axios.get(`${API_BASE_URL}/api/playlists`)
    } else {
      response = await axios.get(`${API_BASE_URL}/api/playlists/category/${category}`)
    }
    
    if (response.data.code === 200) {
      categoryPlaylists.value = response.data.data || []
    } else {
      ElMessage.error(response.data.message || '获取分类歌单失败')
    }
  } catch (err) {
    console.error('获取分类歌单失败:', err)
    ElMessage.error('网络错误，请稍后再试')
  } finally {
    loadingPlaylists.value = false
  }
}

// 导航到歌单详情
const navigateToPlaylist = (playlistId) => {
  router.push(`/playlist/${playlistId}`)
}

// 格式化播放次数
const formatPlayCount = (count) => {
  if (!count && count !== 0) return '0'
  if (count < 10000) return count
  return (count / 10000).toFixed(1) + '万'
}

// 生成图片URL
const getImageUrl = (path) => {
  const defaultCover = 'http://localhost:8085/public/image/default-cover.jpg'
  if (!path) return defaultCover
  
  if (path.startsWith('http')) {
    return path
  } else {
    // 确保路径前面有/public/image/
    if (!path.includes('/public/image/')) {
      path = '/public/image/' + path
    }
    return `${API_BASE_URL}${path.startsWith('/') ? '' : '/'}${path}`
  }
}

onMounted(() => {
  fetchCategories()
})

// 监听分类变化
watch(() => activeCategory.value, (newCategory) => {
  if (newCategory) {
    fetchPlaylistsByCategory(newCategory)
  }
})
</script>

<style scoped>
.category-view {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  margin-bottom: 25px;
  font-weight: bold;
}

.loading-container, .error-message, .loading-playlists, .empty-playlist {
  padding: 30px;
  text-align: center;
}

.error-message {
  color: #f56c6c;
}

.category-tabs {
  margin-bottom: 30px;
  overflow-x: auto;
  border-bottom: 1px solid #e6e6e6;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  white-space: nowrap;
}

.category-list li {
  padding: 10px 22px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: color 0.2s ease, border-bottom 0.2s ease;
  color: #666;
  font-size: 15px;
}

.category-list li:hover {
  color: #333;
}

.category-list li.active {
  border-bottom: 2px solid #ec4141;
  color: #ec4141;
  font-weight: 500;
}

.category-list li:first-child {
  font-weight: 500;
}

.playlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 30px 18px;
}

.playlist-card {
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.playlist-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.playlist-cover {
  position: relative;
  height: 0;
  padding-bottom: 100%; /* 1:1 ratio */
  overflow: hidden;
}

.playlist-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.playlist-play-count {
  position: absolute;
  top: 0;
  right: 0;
  padding: 4px 8px;
  background-color: rgba(0, 0, 0, 0.4);
  color: #fff;
  font-size: 12px;
  border-radius: 0 0 0 8px;
  display: flex;
  align-items: center;
  gap: 3px;
}

.playlist-play-count .el-icon {
  font-size: 12px;
}

.playlist-overlay {
  position: absolute;
  bottom: 10px;
  right: 10px;
  width: 35px;
  height: 35px;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.playlist-card:hover .playlist-overlay {
  opacity: 1;
}

.playlist-overlay .el-icon {
  font-size: 18px;
  color: white;
}

.playlist-info {
  padding: 8px 5px;
}

.playlist-name {
  font-size: 14px;
  font-weight: normal;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  height: 2.8em;
}

.playlist-creator {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .playlist-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 15px;
  }
}
</style> 
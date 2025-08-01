<template>
  <div class="song-detail-page">
    <div class="song-info-header">
      <div class="song-cover-container">
        <img :src="getSongCoverUrl(song.coverImg)" :alt="song.name" class="song-cover">
        <div class="play-overlay" @click="playSong">
          <el-icon><VideoPlay /></el-icon>
        </div>
      </div>
      
      <div class="song-info">
        <h1 class="song-title">{{ song.name }}</h1>
        
        <div class="song-artist">
          <router-link 
            v-if="song.artist"
            :to="'/artist/' + encodeURIComponent(song.artist)"
            class="artist-link"
          >
            歌手：{{ song.artist }}
          </router-link>
          <span v-else>未知歌手</span>
        </div>
        
        <div class="song-meta-info">
          <div class="song-meta-item">
            <span class="meta-label">专辑：</span>
            <span class="meta-value">{{ song.album || '未知专辑' }}</span>
          </div>
          
          <div class="song-meta-item">
            <span class="meta-label">时长：</span>
            <span class="meta-value">{{ formatTime(song.duration || 0) }}</span>
          </div>
          
          <div class="song-meta-item" v-if="song.playCount">
            <span class="meta-label">播放次数：</span>
            <span class="meta-value">{{ song.playCount }}</span>
          </div>
        </div>
        
        <div class="song-actions">
          <el-button type="primary" @click="playSong">
            <el-icon><VideoPlay /></el-icon> 播放
          </el-button>
          <el-button @click="addToFavorite" v-if="!song.isFavorite">
            <el-icon><Star /></el-icon> 收藏
          </el-button>
          <el-button type="success" @click="removeFromFavorite" v-else>
            <el-icon><StarFilled /></el-icon> 已收藏
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 评论区域 -->
    <div class="comments-section">
      <h2 class="section-title">评论</h2>
      <comment-section :song-id="songId" :song-artist="song.artist" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { usePlayerStore } from '@/stores/playerStore'
import { useUserStore } from '@/stores/userStore'
import { 
  VideoPlay, Star, StarFilled
} from '@element-plus/icons-vue'
import { API_BASE_URL } from '@/api/config'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import CommentSection from '@/components/comment/CommentSection.vue'

const route = useRoute()
const playerStore = usePlayerStore()
const userStore = useUserStore()

const songId = computed(() => Number(route.params.id))
const song = ref({})
const defaultCover = `${API_BASE_URL}/public/image/default-cover.jpg`

// 获取歌曲信息
const loadSongDetails = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/songs/${songId.value}`)
    if (response.data.code === 200) {
      const songData = response.data.data;
      console.log('歌曲数据:', songData);
      console.log('歌曲封面原始路径:', songData.coverImg);
      
      song.value = songData;
      // 检查是否已收藏
      checkIfFavorite();
    } else {
      ElMessage.error('获取歌曲信息失败');
    }
  } catch (error) {
    console.error('获取歌曲详情失败:', error);
    ElMessage.error('网络错误，请稍后再试');
  }
}

// 处理歌曲封面URL
const getSongCoverUrl = (coverImg) => {
  if (!coverImg) {
    return defaultCover;
  }
  
  // 如果已经是完整URL则直接返回
  if (coverImg.startsWith('http://') || coverImg.startsWith('https://')) {
    return coverImg;
  }
  
  // 确保路径格式正确
  let path = coverImg;
  if (!path.startsWith('/')) {
    path = '/' + path;
  }
  
  // 返回完整URL
  const fullUrl = API_BASE_URL + path;
  console.log('处理后的完整封面URL:', fullUrl);
  return fullUrl;
}

// 格式化时间
const formatTime = (seconds) => {
  if (!seconds) return '00:00'
  
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 播放歌曲
const playSong = () => {
  playerStore.playSong(song.value)
}

// 检查是否已收藏
const checkIfFavorite = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const response = await axios.get(`${API_BASE_URL}/favorites/check/${songId.value}`, {
      headers: { token: localStorage.getItem('token') }
    })
    
    if (response.data.code === 200) {
      song.value.isFavorite = response.data.data
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 添加到收藏
const addToFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    console.log(`尝试收藏歌曲ID: ${songId.value}`);
    const response = await axios.post(`${API_BASE_URL}/favorites`, {
      songId: songId.value,
      songName: song.value.name,
      artist: song.value.artist,
      album: song.value.album,
      coverUrl: song.value.coverImg
    }, {
      headers: { token: localStorage.getItem('token') }
    })
    
    console.log('收藏响应:', response.data);
    if (response.data.code === 200) {
      ElMessage.success('收藏成功')
      song.value.isFavorite = true
      
      // 强制刷新收藏状态
      setTimeout(() => {
        checkIfFavorite();
      }, 500);
    } else {
      ElMessage.error(response.data.message || '收藏失败')
    }
  } catch (error) {
    console.error('收藏失败:', error)
    if (error.response) {
      console.error('错误响应:', error.response.data);
    }
    ElMessage.error('网络错误，请稍后再试')
  }
}

// 取消收藏
const removeFromFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    console.log(`尝试取消收藏歌曲ID: ${songId.value}`);
    const response = await axios.delete(`${API_BASE_URL}/favorites/${songId.value}`, {
      headers: { token: localStorage.getItem('token') }
    })
    
    console.log('取消收藏响应:', response.data);
    if (response.data.code === 200) {
      ElMessage.success('已取消收藏')
      song.value.isFavorite = false
    } else {
      ElMessage.error(response.data.message || '取消收藏失败')
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    if (error.response) {
      console.error('错误响应:', error.response.data);
    }
    ElMessage.error('网络错误，请稍后再试')
  }
}

// 页面加载时执行
onMounted(() => {
  loadSongDetails()
  
  // 如果URL包含评论锚点，自动滚动到评论区
  if (route.hash === '#comments') {
    setTimeout(() => {
      const commentsSection = document.querySelector('.comments-section')
      if (commentsSection) {
        commentsSection.scrollIntoView({ behavior: 'smooth' })
      }
    }, 1000) // 延迟以确保内容加载完成
  }
})
</script>

<style scoped>
.song-detail-page {
  padding: 15px;
  max-width: 1000px;
  margin: 0 auto;
}

.song-info-header {
  display: flex;
  margin-bottom: 30px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  padding: 15px;
}

.song-cover-container {
  position: relative;
  width: 180px;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 20px;
  flex-shrink: 0;
  background-color: #f0f0f0; /* 添加背景色作为图片未加载时的占位 */
}

.song-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.2s;
  cursor: pointer;
}

.play-overlay .el-icon {
  font-size: 40px;
  color: white;
}

.song-cover-container:hover .play-overlay {
  opacity: 1;
}

.song-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.song-title {
  font-size: 24px;
  margin: 0 0 10px;
  color: #333;
}

.song-artist {
  margin-bottom: 15px;
  font-size: 15px;
}

.artist-link {
  color: #ec4141;
  text-decoration: none;
}

.song-meta-info {
  margin-bottom: 20px;
}

.song-meta-item {
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.meta-label {
  font-weight: 500;
  margin-right: 5px;
}

.song-actions {
  margin-top: auto;
  display: flex;
  gap: 8px;
}

.song-actions .el-button {
  padding: 8px 15px;
  font-size: 14px;
}

.section-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.comments-section {
  margin-bottom: 30px;
}

.playlist-selection {
  max-height: 360px;
  overflow-y: auto;
}

.playlist-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}

.playlist-item {
  display: flex;
  flex-direction: column;
  border-radius: 6px;
  overflow: hidden;
  background: #f9f9f9;
  cursor: pointer;
  transition: transform 0.2s;
}

.playlist-item:hover {
  transform: translateY(-3px);
}

.playlist-cover {
  width: 100%;
  aspect-ratio: 1/1;
  object-fit: cover;
}

.playlist-info {
  padding: 8px;
}

.playlist-info h4 {
  margin: 0 0 4px;
  font-size: 13px;
}

.playlist-info p {
  margin: 0;
  color: #999;
  font-size: 11px;
}

.no-playlists {
  text-align: center;
  padding: 20px 0;
}

.loading-container {
  padding: 15px;
}

@media (max-width: 600px) {
  .song-info-header {
    flex-direction: column;
  }
  
  .song-cover-container {
    width: 100%;
    max-width: 180px;
    height: auto;
    aspect-ratio: 1/1;
    margin: 0 auto 15px;
  }
  
  .song-actions {
    flex-wrap: wrap;
  }
  
  .song-actions .el-button {
    flex: 1;
    min-width: 45%;
  }
}
</style>
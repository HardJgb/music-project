<template>
  <div class="song-list-view">
    <h1 class="page-title">歌单</h1>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-else class="song-grid">
      <div v-for="song in songs" :key="song.id" class="song-card">
        <div class="song-cover" @click="playSong(song)">
          <img :src="song.coverImg || defaultCover" :alt="song.name">
          <div class="play-overlay">
            <el-icon><VideoPlay /></el-icon>
          </div>
          <div class="song-stats">
            <el-icon><Headset /></el-icon>
            <span>{{ formatPlayCount(song.playCount || 0) }}</span>
          </div>
        </div>
        <div class="song-info">
          <div class="song-name" @click="playSong(song)">{{ song.name }}</div>
          <div class="song-artist">{{ song.artist }}</div>
        </div>
        <div class="song-actions">
          <el-button circle size="small" @click="playSong(song)">
            <el-icon><VideoPlay /></el-icon>
          </el-button>
          <el-button circle size="small" @click="addToPlayQueue(song)">
            <el-icon><Plus /></el-icon>
          </el-button>
          <el-button circle size="small" @click="toggleFavorite(song)" :class="{ 'is-favorite': isFavorite(song.id) }">
            <el-icon><Star /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { API_BASE_URL, getAudioUrl } from '@/api/config'
import { usePlayerStore } from '@/stores/playerStore'
import { useUserStore } from '@/stores/userStore'
import { VideoPlay, Plus, Star, Headset } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const songs = ref([])
const loading = ref(true)
const error = ref(null)
const favorites = ref([])
const defaultCover = 'http://localhost:8085/public/image/default-cover.jpg'

const playerStore = usePlayerStore()
const userStore = useUserStore()

// 生成正确的图片URL
const getImageUrl = (path) => {
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

// 获取所有歌曲
const fetchSongs = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await axios.get(`${API_BASE_URL}/api/songs`)
    
    if (response.data.code === 200) {
      // 处理每首歌曲的图片和音频URL
      songs.value = (response.data.data || []).map(song => ({
        ...song,
        coverImg: getImageUrl(song.coverImg || song.coverUrl),
        fileUrl: song.fileUrl
      }))
      console.log('获取到的歌曲列表:', songs.value)
    } else {
      error.value = response.data.message || '获取歌曲列表失败'
    }
  } catch (err) {
    console.error('获取歌曲列表失败:', err)
    error.value = '网络错误，请稍后再试'
  } finally {
    loading.value = false
  }
}

// 获取收藏歌曲列表
const fetchFavorites = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const response = await axios.get(`${API_BASE_URL}/favorites`, {
      headers: { token: userStore.token }
    })
    
    if (response.data.code === 200) {
      favorites.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
  }
}

// 播放单首歌曲
const playSong = (song) => {
  if (!song) return
  
  // 确保音频URL格式正确
  let audioUrl = song.fileUrl
  if (!audioUrl) {
    audioUrl = `/public/image/${song.name}.mp3`
  }
  
  const songObj = {
    id: song.id,
    name: song.name,
    artist: song.artist,
    album: song.album || '未知专辑',
    cover: song.coverImg || defaultCover,
    fileUrl: getAudioUrl(audioUrl)
  }
  
  console.log('播放歌曲:', songObj)
  playerStore.playSong(songObj)
  ElMessage.success(`正在播放: ${song.name}`)
  
  // 调用增加播放次数的API
  incrementSongPlayCount(song.id)
}

// 调用增加播放次数的API
const incrementSongPlayCount = async (songId) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/songs/${songId}/play`)
    console.log('增加播放次数结果:', response.data)
    
    // 更新本地歌曲的播放次数
    if (response.data.code === 200 && response.data.data) {
      const updatedSong = response.data.data;
      // 找到并更新对应歌曲的播放次数
      const songIndex = songs.value.findIndex(s => s.id === songId);
      if (songIndex !== -1) {
        songs.value[songIndex].playCount = updatedSong.playCount;
      }
    }
  } catch (error) {
    console.error('增加播放次数失败:', error)
  }
}

// 添加到播放队列
const addToPlayQueue = (song) => {
  if (!song) return
  
  // 确保音频URL格式正确
  let audioUrl = song.fileUrl
  if (!audioUrl) {
    audioUrl = `/public/image/${song.name}.mp3`
  }
  
  const songObj = {
    id: song.id,
    name: song.name,
    artist: song.artist,
    album: song.album || '未知专辑',
    cover: song.coverImg || defaultCover,
    fileUrl: getAudioUrl(audioUrl)
  }
  
  playerStore.addToPlaylist(songObj)
  ElMessage.success(`已添加到播放队列: ${song.name}`)
}

// 收藏/取消收藏歌曲
const toggleFavorite = async (song) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  const isSongFavorite = isFavorite(song.id)
  
  try {
    if (isSongFavorite) {
      // 取消收藏
      const response = await axios.delete(`${API_BASE_URL}/favorites/${song.id}`, {
        headers: { token: userStore.token }
      })
      
      if (response.data.code === 200) {
        favorites.value = favorites.value.filter(f => f.songId !== song.id)
        ElMessage.success('已取消收藏')
      } else {
        ElMessage.error(response.data.message || '取消收藏失败')
      }
    } else {
      // 添加收藏
      const response = await axios.post(`${API_BASE_URL}/favorites`, {
        songId: song.id,
        songName: song.name,
        artist: song.artist,
        album: song.album || '',
        coverUrl: song.coverImg || ''
      }, {
        headers: { token: userStore.token }
      })
      
      if (response.data.code === 200) {
        favorites.value.push({
          songId: song.id,
          songName: song.name,
          artist: song.artist,
          coverUrl: song.coverImg,
          createdAt: new Date().toISOString()
        })
        ElMessage.success('已收藏')
      } else {
        ElMessage.error(response.data.message || '收藏失败')
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后再试')
  }
}

// 检查歌曲是否已收藏
const isFavorite = (songId) => {
  return favorites.value.some(f => f.songId === songId)
}

// 格式化播放次数
const formatPlayCount = (count) => {
  if (!count && count !== 0) return '0'
  if (count < 10000) return count
  return (count / 10000).toFixed(1) + '万'
}

onMounted(() => {
  fetchSongs()
  fetchFavorites()
})
</script>

<style scoped>
.song-list-view {
  padding: 20px;
}

.page-title {
  font-size: 28px;
  margin-bottom: 30px;
  font-weight: bold;
}

.loading-container, .error-message {
  padding: 30px;
  text-align: center;
}

.error-message {
  color: #f56c6c;
}

.song-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.song-card {
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.song-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.song-cover {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  overflow: hidden;
  cursor: pointer;
}

.song-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.song-cover:hover img {
  transform: scale(1.05);
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.song-cover:hover .play-overlay {
  opacity: 1;
}

.play-overlay .el-icon {
  font-size: 48px;
  color: #fff;
}

.song-stats {
  position: absolute;
  bottom: 0;
  right: 0;
  padding: 5px 8px;
  background-color: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.song-info {
  padding: 12px;
}

.song-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 5px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-name:hover {
  color: #ec4141;
}

.song-artist {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-actions {
  display: flex;
  justify-content: space-around;
  padding: 0 10px 12px;
}

.song-actions .el-button {
  transition: all 0.2s ease;
}

.song-actions .el-button:hover {
  color: #ec4141;
  background-color: rgba(236, 65, 65, 0.1);
}

.is-favorite {
  color: #ec4141 !important;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .song-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }
}
</style> 
<template>
  <div class="ranking-view">
    <h1 class="page-title">音乐排行榜</h1>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-else class="ranking-content">
      <!-- 顶部榜单 - 前三名 -->
      <div class="top-three-section" v-if="rankedSongs.length >= 3">
        <div class="top-three-header">
          <h2>热门歌曲TOP3</h2>
          <p>最受欢迎的音乐</p>
        </div>
        
        <div class="top-three-cards">
          <div v-for="(song, index) in rankedSongs.slice(0, 3)" :key="song.id" class="top-card">
            <div class="top-rank">{{ index + 1 }}</div>
            <div class="top-card-content" @click="playSong(song)">
              <div class="top-cover">
                <img :src="song.coverImg || defaultCover" :alt="song.name">
                <div class="play-btn">
                  <el-icon><VideoPlay /></el-icon>
                </div>
              </div>
              <div class="top-info">
                <div class="top-name">{{ song.name }}</div>
                <div class="top-artist">{{ song.artist }}</div>
                <div class="top-plays">
                  <el-icon><Headset /></el-icon>
                  <span>{{ formatPlayCount(song.playCount || 0) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 榜单列表 -->
      <div class="ranking-list">
        <h2>完整榜单</h2>
        
        <el-table :data="rankedSongs" stripe style="width: 100%" @row-dblclick="playSong">
          <el-table-column type="index" width="80">
            <template #default="scope">
              <div class="rank-number" :class="{'top-three': scope.$index < 3}">{{ scope.$index + 1 }}</div>
            </template>
          </el-table-column>
          
          <el-table-column label="歌曲" min-width="300">
            <template #default="scope">
              <div class="song-info">
                <div class="song-cover">
                  <img :src="scope.row.coverImg || defaultCover" :alt="scope.row.name">
                </div>
                <div class="song-detail">
                  <div class="song-name">{{ scope.row.name }}</div>
                  <div class="song-artist">{{ scope.row.artist }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="播放次数" width="150">
            <template #default="scope">
              <div class="play-count-display">
                <el-icon><Headset /></el-icon>
                <span>{{ formatPlayCount(scope.row.playCount || 0) }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="时长" width="120">
            <template #default="scope">
              {{ formatDuration(scope.row.duration) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button circle size="small" @click="playSong(scope.row)">
                <el-icon><VideoPlay /></el-icon>
              </el-button>
              <el-button circle size="small" @click="addToPlayQueue(scope.row)">
                <el-icon><Plus /></el-icon>
              </el-button>
              <el-button 
                circle 
                size="small" 
                @click="toggleFavorite(scope.row)"
                :class="{ 'is-favorite': isFavorite(scope.row.id) }"
              >
                <el-icon><Star /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { API_BASE_URL, getAudioUrl } from '@/api/config'
import { usePlayerStore } from '@/stores/playerStore'
import { useUserStore } from '@/stores/userStore'
import { VideoPlay, Plus, Star, Headset } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const songs = ref([])
const loading = ref(true)
const error = ref(null)
const favorites = ref([])
const defaultCover = 'http://localhost:8085/public/image/default-cover.jpg'

const playerStore = usePlayerStore()
const userStore = useUserStore()
const router = useRouter()

// 根据播放次数排序的歌曲
const rankedSongs = computed(() => {
  return [...songs.value].sort((a, b) => {
    const countA = a.playCount || 0
    const countB = b.playCount || 0
    return countB - countA // 降序排序（从高到低）
  })
})

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
    // 使用专门的排行榜API
    const response = await axios.get(`${API_BASE_URL}/api/songs/ranking`)
    
    if (response.data.code === 200) {
      // 处理每首歌曲的图片和音频URL
      songs.value = (response.data.data || []).map(song => ({
        ...song,
        coverImg: getImageUrl(song.coverImg || song.coverUrl),
        fileUrl: song.fileUrl
      }))
      console.log('获取到的歌曲排行榜:', songs.value)
    } else {
      error.value = response.data.message || '获取排行榜失败'
    }
  } catch (err) {
    console.error('获取排行榜失败:', err)
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
    ElMessageBox.confirm('请先登录后才能收藏歌曲', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
      // 用户取消登录，不做任何处理
    })
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

// 格式化时长
const formatDuration = (seconds) => {
  console.log('RankingView 格式化时长:', seconds, '类型:', typeof seconds)
  if (!seconds) return '00:00'
  
  // 检查是否是毫秒格式，如果大于10000，认为是毫秒格式
  if (seconds > 10000) {
    console.log('检测到毫秒格式，转换:', seconds, '->', Math.floor(seconds/1000))
    seconds = Math.floor(seconds / 1000) // 转换为秒
  }
  
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  const result = `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
  console.log('格式化结果:', result)
  return result
}

onMounted(() => {
  fetchSongs()
  fetchFavorites()
})
</script>

<style scoped>
.ranking-view {
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

/* 顶部热门歌曲样式 */
.top-three-section {
  margin-bottom: 40px;
}

.top-three-header {
  margin-bottom: 20px;
}

.top-three-header h2 {
  font-size: 24px;
  margin-bottom: 5px;
  color: #ec4141;
}

.top-three-header p {
  color: #666;
  font-size: 14px;
}

.top-three-cards {
  display: flex;
  gap: 20px;
}

.top-card {
  flex: 1;
  position: relative;
  background-color: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.top-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.top-rank {
  position: absolute;
  top: 10px;
  left: 10px;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ec4141;
  color: white;
  font-weight: bold;
  border-radius: 50%;
  z-index: 2;
  font-size: 18px;
}

.top-card-content {
  cursor: pointer;
}

.top-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.top-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.top-card:hover .top-cover img {
  transform: scale(1.05);
}

.play-btn {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.3);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.top-card:hover .play-btn {
  opacity: 1;
}

.play-btn .el-icon {
  font-size: 48px;
  color: white;
}

.top-info {
  padding: 15px;
}

.top-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.top-artist {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.top-plays {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #ec4141;
  font-size: 14px;
}

/* 榜单列表样式 */
.ranking-list {
  background-color: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.ranking-list h2 {
  font-size: 20px;
  margin-bottom: 20px;
}

.rank-number {
  font-size: 16px;
  font-weight: bold;
  color: #666;
}

.rank-number.top-three {
  color: #ec4141;
  font-size: 18px;
}

.song-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.song-cover {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  overflow: hidden;
}

.song-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.song-detail {
  display: flex;
  flex-direction: column;
}

.song-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.song-artist {
  font-size: 12px;
  color: #666;
}

.play-count-display {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
}

.play-count-display .el-icon {
  font-size: 14px;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__row:hover) {
  background-color: #f8f8f8;
}

:deep(.el-table__row:hover .song-name) {
  color: #ec4141;
}

/* 按钮样式 */
.el-button.is-favorite {
  color: #ec4141;
  background-color: rgba(236, 65, 65, 0.1);
}

/* 响应式样式 */
@media (max-width: 768px) {
  .top-three-cards {
    flex-direction: column;
  }
  
  .top-cover {
    height: 140px;
  }
}
</style> 
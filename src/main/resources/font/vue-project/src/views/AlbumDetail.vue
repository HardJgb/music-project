<template>
  <div class="album-detail">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-else class="album-container">
      <div class="album-header">
        <div class="album-cover">
          <img :src="getImageUrl(album.coverImg)" :alt="album.name">
        </div>
        <div class="album-info">
          <h1 class="album-name">{{ album.name }}</h1>
          <div class="album-meta">
            <div class="artist-name">
              <router-link :to="`/artist/${encodeURIComponent(album.artist)}`">{{ album.artist }}</router-link>
            </div>
            <div class="album-date" v-if="album.releaseDate">
              发行时间：{{ formatDate(album.releaseDate) }}
            </div>
            <div class="song-count">
              共{{ album.songs ? album.songs.length : 0 }}首歌曲
            </div>
          </div>
          <div class="album-description" v-if="album.description">
            <p>{{ album.description }}</p>
          </div>
        </div>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" @click="playAllSongs" :disabled="!album.songs || album.songs.length === 0">
          <el-icon><VideoPlay /></el-icon>播放全部
        </el-button>
      </div>
      
      <div v-if="!album.songs || album.songs.length === 0" class="empty-songs">
        <el-empty description="该专辑暂无歌曲" />
      </div>
      
      <div v-else class="songs-list">
        <el-table :data="album.songs" stripe style="width: 100%" @row-dblclick="playSong">
          <el-table-column type="index" width="80" />
          
          <el-table-column label="歌曲" min-width="300">
            <template #default="scope">
              <div class="song-info">
                <div class="song-cover">
                  <img :src="getImageUrl(scope.row.coverImg)" :alt="scope.row.name">
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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_BASE_URL, getAudioUrl, getImageUrl as getConfigImageUrl } from '@/api/config'
import { usePlayerStore } from '@/stores/playerStore'
import { useUserStore } from '@/stores/userStore'
import { VideoPlay, Plus, Star, Headset } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const albumId = computed(() => route.params.id)

const loading = ref(true)
const error = ref(null)
const album = ref({})
const favorites = ref([])
const defaultCover = 'http://localhost:8085/public/image/default-cover.jpg'

const playerStore = usePlayerStore()
const userStore = useUserStore()

// 获取专辑详情
const fetchAlbumDetails = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await axios.get(`${API_BASE_URL}/api/albums/${albumId.value}`)
    
    if (response.data && !response.data.message) {
      album.value = response.data
      
      // 处理每首歌曲的图片
      if (album.value.songs) {
        album.value.songs = album.value.songs.map(song => ({
          ...song,
          coverImg: getImageUrl(song.coverImg || song.coverUrl)
        }))
      }
    } else {
      error.value = response.data.message || '获取专辑详情失败'
    }
  } catch (err) {
    console.error('获取专辑详情失败:', err)
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

// 播放所有歌曲
const playAllSongs = () => {
  if (!album.value.songs || album.value.songs.length === 0) return
  
  // 准备播放列表
  const playlist = album.value.songs.map(song => {
    const audioUrl = song.fileUrl || `/public/image/${song.name}.mp3`
    
    return {
      id: song.id,
      name: song.name,
      artist: song.artist,
      album: album.value.name,
      cover: song.coverImg || album.value.coverImg || defaultCover,
      fileUrl: getAudioUrl(audioUrl)
    }
  })
  
  // 播放第一首，并将其他歌曲加入播放列表
  if (playlist.length > 0) {
    playerStore.playSong(playlist[0])
    
    // 将剩余歌曲添加到播放列表
    if (playlist.length > 1) {
      for (let i = 1; i < playlist.length; i++) {
        playerStore.addToPlaylist(playlist[i])
      }
    }
    
    // 调用增加播放次数的API
    incrementSongPlayCount(playlist[0].id)
    
    ElMessage.success(`正在播放: ${playlist[0].name}`)
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
    album: album.value.name,
    cover: song.coverImg || album.value.coverImg || defaultCover,
    fileUrl: getAudioUrl(audioUrl)
  }
  
  // 播放歌曲
  playerStore.playSong(songObj)
  ElMessage.success(`正在播放: ${song.name}`)
  
  // 调用增加播放次数的API
  incrementSongPlayCount(song.id)
}

// 调用增加播放次数的API
const incrementSongPlayCount = async (songId) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/songs/${songId}/play`)
    
    // 更新本地歌曲的播放次数
    if (response.data.code === 200 && response.data.data) {
      const updatedSong = response.data.data
      // 找到并更新对应歌曲的播放次数
      if (album.value.songs) {
        const songIndex = album.value.songs.findIndex(s => s.id === songId)
        if (songIndex !== -1) {
          album.value.songs[songIndex].playCount = updatedSong.playCount
        }
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
    album: album.value.name,
    cover: song.coverImg || album.value.coverImg || defaultCover,
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
        album: album.value.name || '',
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
  if (!seconds) return '00:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 生成正确的图片URL
const getImageUrl = (path) => {
  return getConfigImageUrl(path) || defaultCover
}

onMounted(() => {
  fetchAlbumDetails()
  fetchFavorites()
})
</script>

<style scoped>
.album-detail {
  padding: 20px;
}

.loading-container, .error-message, .empty-songs {
  padding: 30px;
  text-align: center;
}

.error-message {
  color: #f56c6c;
}

.album-container {
  margin-top: 20px;
}

.album-header {
  display: flex;
  margin-bottom: 30px;
}

.album-cover {
  width: 200px;
  height: 200px;
  margin-right: 30px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.album-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.album-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.album-name {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 15px 0;
}

.album-meta {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
}

.artist-name {
  margin-bottom: 8px;
}

.artist-name a {
  color: #ec4141;
  text-decoration: none;
}

.artist-name a:hover {
  text-decoration: underline;
}

.album-date, .song-count {
  margin-bottom: 5px;
}

.album-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  max-width: 800px;
}

.action-buttons {
  margin-bottom: 20px;
}

/* 歌曲列表样式 */
.songs-list {
  margin-top: 20px;
}

.song-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.song-cover {
  width: 40px;
  height: 40px;
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
}

.song-artist {
  font-size: 12px;
  color: #666;
  margin-top: 3px;
}

.play-count-display {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
}

/* 按钮样式 */
:deep(.el-button.is-favorite) {
  color: #ec4141;
}

/* 表格样式增强 */
:deep(.el-table) {
  --el-table-header-bg-color: #f8f8f8;
  --el-table-border-color: #ebeef5;
  --el-table-row-hover-bg-color: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  font-weight: 600;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover .song-name) {
  color: #ec4141;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .album-header {
    flex-direction: column;
  }
  
  .album-cover {
    width: 180px;
    height: 180px;
    margin: 0 auto 20px;
  }
  
  .album-info {
    text-align: center;
  }
  
  .album-description {
    max-width: 100%;
  }
}
</style> 
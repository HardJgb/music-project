<template>
  <div class="most-played-songs">
    <div class="section-header">
      <h2 class="section-title">推荐歌曲</h2>
      <router-link to="/user/recent-plays" class="view-more">查看更多</router-link>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="songs.length > 0" class="songs-list">
      <div
        v-for="(song, index) in songs"
        :key="song.id"
        class="song-item"
        @dblclick="playSong(song)"
      >
        <div class="song-rank">{{ index + 1 }}</div>
        <el-image
          :src="getProcessedImageUrl(song.coverImg)"
          class="song-cover"
          fit="cover"
        />
        <div class="song-info">
          <div class="song-name" @click="goToSongDetail(song)">{{ song.name }}</div>
          <div class="song-artist">{{ song.artist }}</div>
        </div>
        <div class="song-actions">
          <el-button circle size="small" @click="playSong(song)">
            <el-icon><VideoPlay /></el-icon>
          </el-button>
          <el-button circle size="small" @click="toggleFavorite(song)">
            <el-icon v-if="song.isFavorite" class="star-filled"><StarFilled /></el-icon>
            <el-icon v-else><Star /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-state">
      <el-empty description="暂无推荐歌曲" :image-size="60">
        <template #description>
          <p>暂无推荐歌曲，快去发现好音乐吧!</p>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMostPlayedSongs } from '@/api/userSongs'
import { usePlayerStore } from '@/stores/playerStore'
import { useUserStore } from '@/stores/userStore'
import { useRouter } from 'vue-router'
import { API_BASE_URL } from '@/api/config'
import { VideoPlay, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()
const songs = ref([])
const loading = ref(true)

// 检查歌曲收藏状态
const checkSongsFavoriteStatus = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const promises = songs.value.map(song => 
      axios.get(`${API_BASE_URL}/favorites/check/${song.id}`, {
        headers: { token: localStorage.getItem('token') }
      })
    )
    
    const results = await Promise.all(promises)
    songs.value = songs.value.map((song, index) => ({
      ...song,
      isFavorite: results[index].data.data
    }))
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 获取用户最常播放的歌曲
const fetchMostPlayedSongs = async () => {
  loading.value = true
  try {
    const response = await getMostPlayedSongs()
    if (response.data.code === 200) {
      songs.value = response.data.data || []
      await checkSongsFavoriteStatus()
    } else {
      console.error('获取推荐歌曲失败:', response.data.msg)
    }
  } catch (error) {
    console.error('获取推荐歌曲失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理图片URL
const getProcessedImageUrl = (url) => {
  if (!url) {
    return `${API_BASE_URL}/public/image/default-cover.jpg`
  }

  if (url.startsWith('http')) {
    return url
  }
  
  return `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
}

// 播放歌曲
const playSong = (song) => {
  playerStore.playSong({
    ...song,
    cover: getProcessedImageUrl(song.coverImg),
    fileUrl: song.fileUrl ? 
      (song.fileUrl.startsWith('http') ? song.fileUrl : `${API_BASE_URL}/${song.fileUrl.replace(/^\/+/, '')}`) : 
      `${API_BASE_URL}/public/music/${song.name}.mp3`
  })
}

// 跳转到歌曲详情页
const goToSongDetail = (song) => {
  router.push(`/song/${song.id}`)
}

// 收藏/取消收藏歌曲
const toggleFavorite = async (song) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    if (song.isFavorite) {
      // 已收藏，执行取消收藏
      const response = await axios.delete(`${API_BASE_URL}/favorites/${song.id}`, {
        headers: { token: localStorage.getItem('token') }
      })
      
      if (response.data.code === 200) {
        song.isFavorite = false
        ElMessage.success('已取消收藏')
      } else {
        ElMessage.error(response.data.message || '取消收藏失败')
      }
    } else {
      // 未收藏，执行收藏
      const response = await axios.post(`${API_BASE_URL}/favorites`, {
        songId: song.id,
        songName: song.name,
        artist: song.artist,
        album: song.album,
        coverUrl: song.coverImg
      }, {
        headers: { token: localStorage.getItem('token') }
      })
      
      if (response.data.code === 200) {
        song.isFavorite = true
        ElMessage.success('收藏成功')
      } else {
        ElMessage.error(response.data.message || '收藏失败')
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('网络错误，请稍后再试')
  }
}

onMounted(() => {
  fetchMostPlayedSongs()
})
</script>

<style scoped>
.most-played-songs {
  margin-top: 40px;
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.view-more {
  color: #666;
  font-size: 14px;
  text-decoration: none;
}

.view-more:hover {
  color: #ec4141;
}

.loading-container {
  padding: 10px 0;
}

.songs-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.song-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  background-color: #f5f5f5;
  transition: all 0.2s ease;
  cursor: pointer;
}

.song-item:hover {
  background-color: #eaeaea;
}

.song-rank {
  font-size: 16px;
  font-weight: bold;
  color: #999;
  width: 24px;
  text-align: center;
  margin-right: 12px;
}

.song-item:nth-child(1) .song-rank {
  color: #ff4500;
}

.song-item:nth-child(2) .song-rank {
  color: #ff8c00;
}

.song-item:nth-child(3) .song-rank {
  color: #ffd700;
}

.song-cover {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  margin-right: 12px;
}

.song-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.song-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
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
  gap: 8px;
}

.song-actions .el-button {
  padding: 6px;
}

/* 添加收藏按钮高亮样式 */
.song-actions .el-button :deep(.star-filled) {
  color: #ec4141;
}

.song-actions .el-button:hover :deep(.star-filled) {
  color: #ec4141;
}

.empty-state {
  padding: 20px 0;
  color: #909399;
}
</style> 
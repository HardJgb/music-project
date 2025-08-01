<template>
  <div class="favorite-music">
    <div class="page-header">
      <h1>我喜欢的音乐</h1>
      <div class="header-stats">
        <span>{{ favorites.length }}首歌曲</span>
        <span v-if="favorites.length > 0">· {{ formatPlayTime(totalPlayTime) }}分钟</span>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="favorites.length === 0" class="empty-container">
      <el-empty description="还没有收藏歌曲" />
      <el-button type="primary" @click="$router.push('/discover')">去发现音乐</el-button>
    </div>

    <div v-else class="favorite-songs">
      <el-button type="primary" @click="playAllSongs" class="play-all-btn">
        <el-icon><VideoPlay /></el-icon> 播放全部
      </el-button>

      <!-- 使用表格显示收藏歌曲 -->
      <el-table 
        :data="favorites" 
        style="width: 100%" 
        @row-dblclick="playSong">
        <!-- 序号列 -->
        <el-table-column width="60">
          <template #default="scope">
            <span class="song-index">{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        
        <!-- 歌曲信息列 -->
        <el-table-column label="歌曲">
          <template #default="scope">
            <div class="song-info">
              <img 
                :src="scope.row.coverUrl || scope.row.cover || scope.row.coverImg || defaultCover" 
                :alt="scope.row.songName" 
                class="song-cover"
                @error="handleImageError(scope.row)"
              >
              <div class="song-detail">
                <div class="song-name">{{ scope.row.songName }}</div>
                <div class="song-artist">{{ scope.row.artist }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <!-- 专辑列 -->
        <el-table-column label="专辑" width="200">
          <template #default="scope">
            {{ scope.row.album || '未知专辑' }}
          </template>
        </el-table-column>
        
        <!-- 操作列 -->
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <div class="song-actions">
              <el-button type="primary" circle size="small" @click.stop="playSong(scope.row)">
                <el-icon><VideoPlay /></el-icon>
              </el-button>
              <el-button type="danger" circle size="small" @click.stop="removeFavorite(scope.row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { usePlayerStore } from '@/stores/playerStore'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { API_BASE_URL, getAudioUrl, getImageUrl } from '@/api/config'
import { VideoPlay, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const playerStore = usePlayerStore()
const favorites = ref([])
const loading = ref(true)
const defaultCover = 'http://localhost:8085/public/image/default-cover.jpg'

// 计算总播放时长（假设每首歌约3分钟）
const totalPlayTime = computed(() => favorites.value.length * 3)

// 获取收藏歌曲
const fetchFavorites = async () => {
  loading.value = true
  try {
    // 确保用户已登录
    if (!userStore.isLoggedIn) {
      loading.value = false
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    
    // 修正API路径为正确的后端接口
    const response = await axios.get(`${API_BASE_URL}/favorites`, {
      headers: {
        token: userStore.token
      }
    })
    
    if (response.data.code === 200) {
      // 处理收藏歌曲的数据，确保每首歌都有正确的图片URL
      favorites.value = response.data.data.map(song => ({
        ...song,
        cover: getImageUrl(song.cover),
        coverImg: getImageUrl(song.coverImg || song.coverUrl)
      }))
    } else {
      ElMessage.error(response.data.message || '获取收藏失败')
    }
  } catch (error) {
    console.error('获取收藏歌曲失败:', error)
    ElMessage.error('获取收藏失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 播放单首歌曲
const playSong = (song) => {
  if (!song) return
  
  // 请求API获取完整的歌曲信息
  fetchSongDetails(song.songId)
    .then(songDetails => {
      if (songDetails) {
        const songObj = {
          id: song.songId,
          name: song.songName,
          artist: song.artist,
          album: song.album || '未知专辑',
          cover: song.coverUrl || defaultCover,
          fileUrl: songDetails.fileUrl || getAudioUrl(`/public/image/${song.songName}.mp3`)
        }
        
        console.log('播放歌曲:', songObj)
        playerStore.playSong(songObj)
        ElMessage.success(`正在播放: ${song.songName}`)
        
        // 调用增加播放次数的API
        incrementSongPlayCount(song.songId)
      }
    })
    .catch(error => {
      console.error('获取歌曲详情失败:', error)
      ElMessage.error('获取歌曲详情失败，无法播放')
    })
}

// 获取歌曲详情
const fetchSongDetails = async (songId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/songs/${songId}`)
    if (response.data.code === 200 && response.data.data) {
      return response.data.data
    }
    return null
  } catch (error) {
    console.error('获取歌曲详情失败:', error)
    throw error
  }
}

// 调用增加播放次数的API
const incrementSongPlayCount = async (songId) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/songs/${songId}/play`)
    console.log('增加播放次数结果:', response.data)
    
    // 此处不需要更新本地播放次数，因为收藏页面不显示播放次数
    // 但如果以后需要显示，可以添加相应的更新逻辑
  } catch (error) {
    console.error('增加播放次数失败:', error)
  }
}

// 播放所有收藏的歌曲
const playAllSongs = () => {
  if (favorites.value.length === 0) {
    ElMessage.warning('没有可播放的歌曲')
    return
  }
  
  // 获取所有歌曲详情
  Promise.all(favorites.value.map(song => fetchSongDetails(song.songId)))
    .then(songDetailsList => {
      const songs = songDetailsList.map((details, index) => {
        const song = favorites.value[index]
        return {
          id: song.songId,
          name: song.songName,
          artist: song.artist,
          album: song.album || '未知专辑',
          cover: song.coverUrl || defaultCover,
          fileUrl: details?.fileUrl || getAudioUrl(`/public/image/${song.songName}.mp3`)
        }
      })
      
      if (songs.length > 0) {
        playerStore.setPlayQueue(songs)
        playerStore.playSongAtIndex(0)
        ElMessage.success('正在播放全部收藏歌曲')
      }
    })
    .catch(error => {
      console.error('获取歌曲详情失败:', error)
      ElMessage.error('无法播放全部歌曲，请稍后再试')
    })
}

// 取消收藏
const removeFavorite = async (song) => {
  try {
    const response = await axios.delete(`${API_BASE_URL}/favorites/${song.songId}`, {
      headers: {
        token: userStore.token
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('取消收藏成功')
      fetchFavorites() // 重新获取收藏列表
    } else {
      ElMessage.error(response.data.message || '取消收藏失败')
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('取消收藏失败，请检查网络连接')
  }
}

// 格式化播放时间
const formatPlayTime = (minutes) => {
  if (minutes < 60) return minutes
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}小时${mins > 0 ? ` ${mins}分钟` : ''}`
}

// 处理图片加载错误
const handleImageError = (song) => {
  console.error('图片加载失败:', song.cover || song.coverImg)
  song.cover = defaultCover
  song.coverImg = defaultCover
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
.favorite-music {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  margin-bottom: 10px;
}

.header-stats {
  font-size: 14px;
  color: #666;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
}

.empty-container .el-button {
  margin-top: 20px;
}

.play-all-btn {
  margin-bottom: 20px;
  background-color: #ec4141;
  border-color: #ec4141;
}

.play-all-btn:hover,
.play-all-btn:focus {
  background-color: #d73737;
  border-color: #d73737;
}

/* 歌曲信息样式 */
.song-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.song-cover {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

.song-detail {
  display: flex;
  flex-direction: column;
}

.song-name {
  font-weight: 500;
  margin-bottom: 3px;
}

.song-artist {
  font-size: 12px;
  color: #666;
}

.song-actions {
  display: flex;
  gap: 8px;
}

.song-index {
  color: #909399;
}

/* 表格样式 */
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

.delete-btn:hover {
  color: #ec4141;
  background-color: rgba(236, 65, 65, 0.1);
}
</style> 
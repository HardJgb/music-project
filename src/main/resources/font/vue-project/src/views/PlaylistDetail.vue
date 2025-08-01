<template>
  <div class="playlist-detail">
    <!-- 添加返回按钮 -->
    <div class="back-button-container">
      <el-button 
        :icon="ArrowLeft" 
        circle 
        class="back-button"
        @click="goBack"
      />
      <span class="back-text">返回</span>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    
    <template v-else>
      <!-- 歌单头部信息 -->
      <div class="playlist-header">
        <div class="playlist-cover">
          <img :src="getImageUrl(playlist.coverImg)" :alt="playlist.name">
        </div>
        <div class="playlist-info">
          <h1>{{ playlist.name }}</h1>
          <div class="playlist-stats">
            <span class="play-count">
              <el-icon><Headset /></el-icon> {{ formatPlayCount(playlist.playCount) }}次播放
            </span>
            <span class="create-time">创建于: {{ formatDate(playlist.createTime) }}</span>
          </div>
          <div class="playlist-description">{{ playlist.description }}</div>
          <div class="playlist-actions">
            <el-button type="primary" round @click="playAllSongs">
              <el-icon><VideoPlay /></el-icon> 播放全部
            </el-button>
            <el-button round @click="toggleFavoritePlaylist" :type="isFavoritePlaylist ? 'success' : ''">
              <el-icon><FolderAdd /></el-icon> {{ isFavoritePlaylist ? '已收藏' : '收藏' }}
            </el-button>
            <el-button round>
              <el-icon><Share /></el-icon> 分享
            </el-button>

          </div>
        </div>
      </div>
      
      <!-- 歌曲列表 -->
      <div class="song-list-container">
        <h2>歌曲列表 <span class="song-count">{{ playlist.songs?.length || 0 }}首歌</span></h2>
        
        <el-table 
          :data="playlist.songs" 
          stripe 
          style="width: 100%"
          @row-dblclick="playSong"
        >
          <el-table-column width="80">
            <template #default="{ row, $index }">
              <div class="song-index">
                <span class="index">{{ $index + 1 }}</span>
                <el-icon @click="playSong(row)" class="play-icon"><VideoPlay /></el-icon>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="歌曲">
            <template #default="{ row }">
              <div class="song-info">
                <img 
                  :src="row.cover || row.coverImg || defaultCover" 
                  :alt="row.name" 
                  class="song-cover"
                  @error="handleImageError(row)"
                >
                <div class="song-meta">
                  <div class="song-name">{{ row.name }}</div>
                  <div class="song-artist">{{ row.artist }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="播放次数" width="100">
            <template #default="scope">
              <div class="play-count-display">
                <el-icon><Headset /></el-icon>
                <span>{{ formatPlayCount(scope.row.playCount || 0) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="时长" width="100">
            <template #default="scope">
              {{ formatDuration(scope.row.duration) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
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
    </template>
  </div>


</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { PLAYLIST_API, getAudioUrl, API_BASE_URL } from '@/api/config'
import { usePlayerStore } from '@/stores/playerStore'
import { useUserStore } from '@/stores/userStore'
import { 
  VideoPlay, FolderAdd, Share, Plus, Star, Headset, ArrowLeft
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addFavoritePlaylist, removeFavoritePlaylist, checkFavorite } from '@/api/favoritePlaylist'

const route = useRoute()
const router = useRouter()
const playlist = ref({})
const loading = ref(true)
const error = ref(null)
const playerStore = usePlayerStore()
const userStore = useUserStore()
const favorites = ref([])
const isFavoritePlaylist = ref(false)
const defaultCover = 'http://localhost:8085/public/image/default-cover.jpg'

const fetchPlaylistDetail = async () => {
  const playlistId = route.params.id
  loading.value = true
  error.value = null
  
  try {
    const response = await axios.get(PLAYLIST_API.getPlaylistById(playlistId))
    if (response.data.code === 200) {
      processPlaylistData(response.data.data)
      
      // 在获取歌单详情成功后立即检查是否已收藏
      if (userStore.isLoggedIn) {
        await checkFavoritePlaylist()
      }
    } else {
      error.value = response.data.message || '获取歌单详情失败'
    }
  } catch (err) {
    console.error('获取歌单详情出错:', err)
    error.value = '网络错误，请稍后再试'
  } finally {
    loading.value = false
  }
}

// 格式化播放次数
const formatPlayCount = (count) => {
  if (!count && count !== 0) return '0'
  if (count < 10000) return count
  return (count / 10000).toFixed(1) + '万'
}

// 格式化时长
const formatDuration = (duration) => {
  console.log('PlaylistDetail 格式化时长:', duration, '类型:', typeof duration)
  if (!duration) return '00:00'
  
  // 检查是否是毫秒格式，如果大于10000，认为是毫秒格式
  if (duration > 10000) {
    duration = Math.floor(duration / 1000) // 转换为秒
  }
  
  const minutes = Math.floor(duration / 60)
  const seconds = duration % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

// 处理图片加载错误
const handleImageError = (song) => {
  console.error('图片加载失败:', song.cover || song.coverImg)
  // 记录歌曲的详细信息，帮助调试
  console.log('歌曲详细信息:', {
    id: song.id,
    name: song.name,
    原始cover: song.cover,
    原始coverImg: song.coverImg
  })
  
  // 使用defaultImg作为默认图片
  const defaultImg = 'http://localhost:8085/public/image/default-cover.jpg'
  song.cover = defaultImg
  song.coverImg = defaultImg
  
  console.log('设置默认图片后:', {
    cover: song.cover,
    coverImg: song.coverImg
  })
}

// 获取图片完整URL
const getImageUrl = (path) => {
  // 调试输出
  console.log('getImageUrl 输入路径:', path)
  
  // 设置默认图片
  const defaultImg = 'http://localhost:8085/public/image/default-cover.jpg'
  if (!path) {
    console.log('图片路径为空，使用默认图片:', defaultImg)
    return defaultImg
  }
  
  // 处理已经是完整URL的情况
  if (path.startsWith('http')) {
    console.log('已是完整URL，直接返回:', path)
    return path
  } 
  
  // 确保路径包含/public/image/
  let processedPath = path
  if (!processedPath.includes('/public/image/')) {
    if (processedPath.startsWith('/')) {
      processedPath = '/public/image' + processedPath
    } else {
      processedPath = '/public/image/' + processedPath
    }
  }
  
  // 拼接完整URL
  const finalUrl = `${API_BASE_URL}${processedPath.startsWith('/') ? '' : '/'}${processedPath}`
  console.log(`图片URL处理结果: ${path} -> ${finalUrl}`)
  return finalUrl
}

// 处理获取的歌单数据
const processPlaylistData = (data) => {
  console.log('处理歌单数据，原始歌单封面:', data.coverImg)
  const coverImgUrl = getImageUrl(data.coverImg)
  console.log('处理后的歌单封面URL:', coverImgUrl)
  
  // 处理歌曲封面
  const processedSongs = data.songs.map(song => {
    console.log(`处理歌曲 ${song.name} 的封面, 原始路径:`, song.coverImg)
    const songCoverUrl = getImageUrl(song.coverImg || song.cover)
    console.log(`处理后的歌曲封面URL:`, songCoverUrl)
    
    return {
      ...song,
      cover: songCoverUrl,
      coverImg: songCoverUrl
    }
  })
  
  // 更新歌单数据
  playlist.value = {
    ...data,
    coverImg: coverImgUrl,
    songs: processedSongs
  }
  
  console.log('处理完成的歌单数据:', playlist.value)
}

// 播放歌曲
const playSong = (song) => {
  console.log('原始歌曲数据:', song)
  
  // 确保音频URL格式正确
  let audioUrl = song.fileUrl
  if (!audioUrl) {
    audioUrl = `/public/image/${song.name}.mp3`
  }
  
  // 使用后端返回的实际文件路径，而不是测试URL
  const songWithFullUrl = {
    id: song.id,
    name: song.name,
    artist: song.artist,
    duration: song.duration,
    fileUrl: getAudioUrl(audioUrl),
    cover: getImageUrl(song.coverImg)
  }
  
  console.log('处理后的歌曲数据:', songWithFullUrl)
  console.log('音频文件URL:', songWithFullUrl.fileUrl)
  
  // 播放该歌曲
  playerStore.playSong(songWithFullUrl)
  
  // 调用增加播放次数的API
  incrementSongPlayCount(song.id)
}

// 调用增加播放次数的API
const incrementSongPlayCount = async (songId) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/songs/${songId}/play`)
    console.log('增加播放次数结果:', response.data)
    
    // 更新本地歌曲的播放次数
    if (playlist.value && playlist.value.songs) {
      const songIndex = playlist.value.songs.findIndex(s => s.id === songId);
      if (songIndex !== -1) {
        playlist.value.songs[songIndex].playCount = response.data.data.playCount;
      }
    }
  } catch (error) {
    console.error('增加播放次数失败:', error)
  }
}

// 添加到播放队列
const addToPlayQueue = (song) => {
  // 确保音频URL格式正确
  let audioUrl = song.fileUrl
  if (!audioUrl) {
    audioUrl = `/public/image/${song.name}.mp3`
  }
  
  const songWithFullUrl = {
    id: song.id,
    name: song.name,
    artist: song.artist,
    duration: song.duration,
    fileUrl: getAudioUrl(audioUrl),
    cover: getImageUrl(song.coverImg)
  }
  
  // 添加到播放列表
  playerStore.addToPlaylist(songWithFullUrl)
}

// 播放全部歌曲
const playAllSongs = () => {
  if (!playlist.value.songs || playlist.value.songs.length === 0) return
  
  // 将所有歌曲添加到播放列表
  playlist.value.songs.forEach(song => {
    addToPlayQueue(song)
  })
  
  // 播放第一首歌
  playSong(playlist.value.songs[0])
}

// 添加返回方法
const goBack = () => {
  router.back()
}

// 检查歌曲是否已收藏
const isFavorite = (songId) => {
  return favorites.value.includes(songId)
}

// 切换收藏状态
const toggleFavorite = async (song) => {
  // 检查用户是否登录并输出调试信息
  console.log('用户登录状态:', userStore.isLoggedIn, '用户token:', userStore.token)
  
  if (!userStore.isLoggedIn || !userStore.token) {
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
  
  try {
    if (isFavorite(song.id)) {
      // 如果已收藏，则取消收藏
      console.log('请求取消收藏:', `${API_BASE_URL}/favorites/${song.id}`)
      console.log('发送的token:', userStore.token)
      
      await axios.delete(`${API_BASE_URL}/favorites/${song.id}`, {
        headers: { token: userStore.token }
      })
      
      // 从收藏列表中移除
      favorites.value = favorites.value.filter(id => id !== song.id)
      ElMessage.success('已取消收藏')
    } else {
      // 如果未收藏，则添加收藏
      console.log('请求添加收藏:', `${API_BASE_URL}/favorites`)
      console.log('发送的歌曲数据:', {
        songId: song.id,
        songName: song.name,
        artist: song.artist,
        album: song.album || '',
        coverUrl: song.coverImg ? getImageUrl(song.coverImg) : ''
      })
      console.log('发送的token:', userStore.token)
      
      await axios.post(`${API_BASE_URL}/favorites`, {
        songId: song.id,
        songName: song.name,
        artist: song.artist,
        album: song.album || '',
        coverUrl: song.coverImg ? getImageUrl(song.coverImg) : ''
      }, {
        headers: { token: userStore.token }
      })
      
      // 添加到收藏列表
      favorites.value.push(song.id)
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error.response ? error.response.data : error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 获取用户收藏列表
const fetchUserFavorites = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const response = await axios.get(`${API_BASE_URL}/favorites`, {
      headers: { token: userStore.token }
    })
    
    if (response.data.code === 200) {
      // 提取所有收藏歌曲的ID
      favorites.value = response.data.data.map(fav => fav.songId)
      console.log('获取到的收藏列表:', favorites.value)
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
  }
}

// Check if the playlist is favorited
const checkFavoritePlaylist = async () => {
  if (!userStore.isLoggedIn || !playlist.value.id) {
    console.log('未登录或歌单ID不存在，无法检查收藏状态')
    return
  }

  try {
    console.log('开始检查歌单收藏状态，歌单ID:', playlist.value.id)
    const response = await checkFavorite(playlist.value.id)
    console.log('检查收藏状态响应:', response)
    
    if (response && response.code === 200) {
      isFavoritePlaylist.value = response.data
      console.log('歌单收藏状态:', isFavoritePlaylist.value ? '已收藏' : '未收藏')
    }
  } catch (error) {
    console.error('检查歌单收藏状态失败:', error)
  }
}

// Toggle favorite playlist
const toggleFavoritePlaylist = async () => {
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('请先登录后才能收藏歌单', '提示', {
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

  try {
    if (isFavoritePlaylist.value) {
      // 取消收藏
      const response = await removeFavoritePlaylist(playlist.value.id)
      if (response && response.code === 200) {
        isFavoritePlaylist.value = false
        ElMessage.success(response.msg || '取消收藏成功')
      } else {
        ElMessage.error(response.msg || '取消收藏失败')
      }
    } else {
      // 添加收藏
      const response = await addFavoritePlaylist(playlist.value.id)
      if (response && response.code === 200) {
        isFavoritePlaylist.value = true
        ElMessage.success(response.msg || '收藏成功')
      } else {
        ElMessage.error(response.msg || '收藏失败')
      }
    }
  } catch (error) {
    console.error('收藏歌单操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

onMounted(() => {
  // 检查登录状态和token
  console.log('当前登录状态:', userStore.isLoggedIn, 'token:', userStore.token)
  console.log('localStorage中的token:', localStorage.getItem('token'))
  
  // 获取歌单详情 (将会在成功后自动检查收藏状态)
  fetchPlaylistDetail()
  
  // 获取用户收藏的歌曲列表
  fetchUserFavorites()
})
</script>

<style scoped>
.playlist-detail {
  padding: 20px;
  color: #333;
}

.loading, .error-message {
  margin: 20px;
  padding: 40px;
  text-align: center;
}

.error-message {
  color: #f56c6c;
}

.playlist-header {
  display: flex;
  margin-bottom: 30px;
  gap: 30px;
}

.playlist-cover {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.playlist-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.playlist-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.playlist-info h1 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 15px;
}

.playlist-stats {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.playlist-description {
  color: #666;
  margin-bottom: 20px;
  line-height: 1.5;
}

.playlist-actions {
  display: flex;
  gap: 10px;
  margin-top: auto;
}

.song-list-container {
  margin-top: 30px;
}

.song-list-container h2 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.song-count {
  font-size: 14px;
  color: #666;
  margin-left: 10px;
  font-weight: normal;
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

/* 按钮样式 */
.playlist-actions .el-button--primary {
  background-color: #ec4141;
  border-color: #ec4141;
}

.playlist-actions .el-button--primary:hover,
.playlist-actions .el-button--primary:focus {
  background-color: #d73737;
  border-color: #d73737;
}

/* 添加响应式布局 */
@media (max-width: 768px) {
  .playlist-header {
    flex-direction: column;
  }
  
  .playlist-cover {
    width: 150px;
    height: 150px;
    margin: 0 auto 20px;
  }
}

.back-button-container {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  cursor: pointer;
}

.back-button {
  background-color: rgba(236, 65, 65, 0.1);
  color: #ec4141;
  border: none;
  font-size: 16px;
  transition: all 0.3s;
}

.back-button:hover {
  background-color: rgba(236, 65, 65, 0.2);
  transform: translateX(-2px);
}

.back-text {
  margin-left: 8px;
  font-size: 14px;
  color: #333;
}

/* 添加收藏图标样式 */
.favorite-icon {
  cursor: pointer;
  font-size: 18px;
  color: #909399;
  transition: all 0.3s;
}

.favorite-icon:hover {
  color: #ec4141;
  transform: scale(1.2);
}

.favorite-icon.is-favorite {
  color: #ec4141;
}

/* 添加收藏按钮的样式 */
.is-favorite {
  color: #ec4141 !important;
  background-color: rgba(236, 65, 65, 0.1);
}
</style> 
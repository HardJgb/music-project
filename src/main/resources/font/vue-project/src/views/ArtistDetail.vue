<template>
  <div class="artist-detail">
    <div class="artist-header">
      <div class="artist-avatar">
        <el-avatar 
          :size="120" 
          :src="getArtistAvatarUrl(artistInfo.avatarUrl)" 
          :icon="UserFilled" 
        />
      </div>
      <div class="artist-info">
        <h1 class="artist-name">{{ decodeURIComponent(artist) }}</h1>
        <div class="song-count" v-if="artistSongs.length">
          <span>{{ artistSongs.length }}首歌曲</span>
          <span v-if="artistAlbums.length"> · {{ artistAlbums.length }}张专辑</span>
        </div>
        <div class="artist-description" v-if="artistInfo.description">
          <p>{{ artistInfo.description }}</p>
        </div>
      </div>
    </div>
    
    <!-- 导航标签 -->
    <div class="artist-nav">
      <div 
        class="nav-item" 
        :class="{ 'active': activeTab === 'songs' }" 
        @click="switchTab('songs')"
      >
        歌曲
      </div>
      <div 
        class="nav-item" 
        :class="{ 'active': activeTab === 'albums' }" 
        @click="switchTab('albums')"
      >
        专辑
      </div>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    
    <!-- 歌曲列表 -->
    <div v-if="activeTab === 'songs'" class="tab-content">
      <div v-if="!artistSongs.length" class="empty-content">
        <el-empty description="该歌手暂无歌曲" />
      </div>
      
      <div v-else class="songs-container">
        <div class="action-buttons">
          <el-button type="primary" @click="playAllSongs" :disabled="artistSongs.length === 0">
            <el-icon><VideoPlay /></el-icon>播放全部
          </el-button>
        </div>
        
        <el-table :data="artistSongs" stripe style="width: 100%" @row-dblclick="playSong">
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
    
    <!-- 专辑列表 -->
    <div v-if="activeTab === 'albums'" class="tab-content">
      <div v-if="!artistAlbums.length" class="empty-content">
        <el-empty description="该歌手暂无专辑" />
      </div>
      
      <div v-else class="albums-container">
        <div class="albums-grid">
          <div v-for="album in artistAlbums" :key="album.id" class="album-card" @click="viewAlbumDetail(album)">
            <div class="album-cover">
              <img :src="getImageUrl(album.coverImg)" :alt="album.name">
            </div>
            <div class="album-info">
              <div class="album-name">{{ album.name }}</div>
              <div class="album-meta">
                <span>{{ album.releaseDate ? formatDate(album.releaseDate) : '未知日期' }}</span>
                <span>{{ album.songCount || 0 }}首歌曲</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_BASE_URL, getAudioUrl } from '@/api/config'
import { usePlayerStore } from '@/stores/playerStore'
import { useUserStore } from '@/stores/userStore'
import { VideoPlay, Plus, Star, Headset, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const artist = computed(() => route.params.artist)

const loading = ref(true)
const error = ref(null)
const artistSongs = ref([])
const artistAlbums = ref([])
const favorites = ref([])
const artistInfo = ref({})
const defaultCover = 'http://localhost:8085/public/image/default-cover.jpg'
const activeTab = ref('songs') // 默认显示歌曲标签

const playerStore = usePlayerStore()
const userStore = useUserStore()

// 切换标签
const switchTab = (tab) => {
  activeTab.value = tab
  
  // 如果切换到专辑标签且还未加载过专辑数据，则加载专辑数据
  if (tab === 'albums' && artistAlbums.value.length === 0) {
    fetchArtistAlbums()
  }
}

// 获取歌手信息
const fetchArtistInfo = async () => {
  try {
    // 先尝试通过名称获取歌手信息
    const response = await axios.get(`${API_BASE_URL}/api/artists/name/${artist.value}`)
    
    if (response.data.code === 200 && response.data.data) {
      artistInfo.value = response.data.data
      console.log('获取到的歌手信息:', artistInfo.value)
      
      // 确保头像URL被正确处理
      if (artistInfo.value.avatarUrl) {
        console.log('原始头像URL:', artistInfo.value.avatarUrl)
      }
    } else {
      // 如果获取失败，设置一个基本的歌手信息对象
      artistInfo.value = {
        name: decodeURIComponent(artist.value),
        avatarUrl: null,
        description: null
      }
    }
  } catch (err) {
    console.error('获取歌手信息失败:', err)
    // 出错时设置基本信息
    artistInfo.value = {
      name: decodeURIComponent(artist.value),
      avatarUrl: null,
      description: null
    }
  }
}

// 获取歌手的歌曲
const fetchSongsByArtist = async () => {
  loading.value = true
  error.value = null
  
  try {
    // 获取歌手信息
    await fetchArtistInfo()
    
    // 获取歌手歌曲
    const response = await axios.get(`${API_BASE_URL}/api/songs/artist/${artist.value}`)
    
    if (response.data.code === 200) {
      artistSongs.value = response.data.data || []
      
      // 处理每首歌曲的图片
      artistSongs.value = artistSongs.value.map(song => ({
        ...song,
        coverImg: getImageUrl(song.coverImg || song.coverUrl)
      }))
    } else {
      error.value = response.data.message || '获取歌手歌曲失败'
    }
  } catch (err) {
    console.error('获取歌手歌曲失败:', err)
    error.value = '网络错误，请稍后再试'
  } finally {
    loading.value = false
  }
}

// 获取歌手的专辑
const fetchArtistAlbums = async () => {
  try {
    // 调用后端API获取歌手专辑
    const response = await axios.get(`${API_BASE_URL}/api/albums/artist/${artist.value}`);
    
    if (response.data) {
      artistAlbums.value = response.data;
      console.log('获取到的专辑列表:', artistAlbums.value);
    } else {
      artistAlbums.value = [];
      console.warn('没有找到该歌手的专辑');
    }
  } catch (error) {
    console.error('获取歌手专辑失败:', error);
    ElMessage.error('获取专辑列表失败');
    artistAlbums.value = [];
  }
}

// 查看专辑详情
const viewAlbumDetail = (album) => {
  // 跳转到专辑详情页
  router.push(`/album/${album.id}`);
  ElMessage.success(`正在查看专辑：${album.name}`);
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
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
  if (!artistSongs.value.length) return
  
  // 准备播放列表
  const playlist = artistSongs.value.map(song => {
    const audioUrl = song.fileUrl || `/public/image/${song.name}.mp3`
    
    return {
      id: song.id,
      name: song.name,
      artist: song.artist,
      album: song.album || '未知专辑',
      cover: song.coverImg || defaultCover,
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
    album: song.album || '未知专辑',
    cover: song.coverImg || defaultCover,
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
      const updatedSong = response.data.data;
      // 找到并更新对应歌曲的播放次数
      const songIndex = artistSongs.value.findIndex(s => s.id === songId);
      if (songIndex !== -1) {
        artistSongs.value[songIndex].playCount = updatedSong.playCount;
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
  if (!seconds) return '00:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 生成正确的图片URL
const getImageUrl = (path) => {
  if (!path) return defaultCover
  
  // 如果URL不是以http开头，添加API基础URL
  if (!path.startsWith('http')) {
    // 确保路径前面有/public/image/
    if (!path.includes('/public/image/')) {
      path = '/public/image/' + path
    }
    return `${API_BASE_URL}${path.startsWith('/') ? '' : '/'}${path}`
  }
  
  return path
}

const defaultAvatar = 'http://localhost:8085/public/image/default-avatar.jpg'

// 处理艺术家头像URL
const getArtistAvatarUrl = (url) => {
  if (!url) {
    console.log('艺术家头像URL为空，使用默认头像')
    return defaultAvatar
  }

  // 如果URL不是以http开头，添加API基础URL
  if (!url.startsWith('http')) {
    // 确保路径前面有/public/image/
    if (!url.includes('/public/image/')) {
      url = '/public/image/' + url
    }
    const processedUrl = `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
    console.log('处理后的艺术家头像URL:', processedUrl)
    return processedUrl
  }
  
  return url
}

onMounted(() => {
  fetchSongsByArtist()
  fetchFavorites()
})
</script>

<style scoped>
.artist-detail {
  padding: 20px;
}

.artist-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.artist-avatar {
  margin-right: 30px;
  background-color: #f5f5f5;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 120px;
  height: auto;
  min-height: 120px;
}

.artist-info {
  display: flex;
  flex-direction: column;
}

.artist-name {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.song-count {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.artist-description {
  font-size: 14px;
  color: #666;
  max-width: 600px;
  line-height: 1.5;
}

/* 导航标签样式 */
.artist-nav {
  display: flex;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.nav-item {
  padding: 12px 20px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  color: #666;
  position: relative;
  transition: color 0.3s;
}

.nav-item:hover {
  color: #ec4141;
}

.nav-item.active {
  color: #ec4141;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #ec4141;
}

.loading-container, .error-message, .empty-content {
  padding: 30px;
  text-align: center;
}

.error-message {
  color: #f56c6c;
}

.action-buttons {
  margin-bottom: 20px;
}

/* 歌曲列表样式 */
.songs-container {
  margin-top: 20px;
  width: 100%;
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

/* 专辑列表样式 */
.albums-container {
  margin-top: 20px;
}

.albums-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 25px;
  padding: 10px 0;
}

.album-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
}

.album-card:hover {
  transform: translateY(-5px);
}

.album-cover {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 10px;
}

.album-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.album-card:hover .album-cover img {
  transform: scale(1.05);
}

.album-info {
  padding: 5px 0;
}

.album-name {
  font-weight: 500;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.album-meta {
  font-size: 12px;
  color: #666;
  display: flex;
  justify-content: space-between;
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
  .artist-header {
    flex-direction: column;
    text-align: center;
  }
  
  .artist-avatar {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .artist-name {
    font-size: 24px;
  }
  
  .artist-description {
    text-align: center;
    max-width: 100%;
  }
  
  .albums-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}
</style> 
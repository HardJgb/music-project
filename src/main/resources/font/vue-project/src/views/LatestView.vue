<template>
  <div class="latest-music-container">
    <h1 class="page-title">最新音乐</h1>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-else>
      <el-table
        :data="songs"
        style="width: 100%"
        @row-dblclick="playSong"
        @row-contextmenu="handleRightClick"
      >
        <el-table-column type="index" width="50" />
        
        <el-table-column label="歌曲" min-width="300">
          <template #default="scope">
            <div class="song-info">
              <el-image
                :src="getProcessedImageUrl(scope.row.coverImg)"
                class="cover-img"
                fit="cover"
                @error="() => logImageError(scope.row)"
              />
              <div class="song-details">
                <div class="song-name">{{ scope.row.name }}</div>
                <div v-if="scope.row.artist" class="song-artist">{{ scope.row.artist }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="时长" width="120">
          <template #default="scope">
            {{ formatDuration(scope.row.duration) }}
          </template>
        </el-table-column>
        
        <el-table-column label="上传时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="播放次数" width="120" sortable>
          <template #default="scope">
            {{ scope.row.playCount || 0 }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button-group>
              <el-button
                size="small"
                @click="playSong(scope.row)"
                :icon="CaretRight"
              />
              <el-button
                size="small"
                @click="addToPlayQueue(scope.row)"
                :icon="Plus"
              />
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 右键菜单弹窗 -->
    <div 
      v-show="contextMenuVisible" 
      class="context-menu" 
      :style="{ top: contextMenuPosition.y + 'px', left: contextMenuPosition.x + 'px' }"
      @click.stop
    >
      <div class="context-menu-item" @click="handleFavorite">
        <el-icon><Star /></el-icon> 
        {{ isFavorite ? '取消收藏' : '收藏歌曲' }}
      </div>
      <div class="context-menu-item" @click="showAddToPlaylistsMenu">
        <el-icon><Document /></el-icon> 添加到歌单
      </div>
      <div v-show="showPlaylistsList" class="context-menu-submenu">
        <template v-if="userPlaylists.length > 0">
          <div 
            v-for="playlist in userPlaylists" 
            :key="playlist.id" 
            class="context-menu-item"
            @click="addToUserPlaylist(playlist.id)"
          >
            {{ playlist.name }}
          </div>
        </template>
        <div v-else class="context-menu-item disabled">
          暂无歌单，请先创建
        </div>
      </div>
    </div>
    
    <!-- 添加到歌单对话框 -->
    <el-dialog
      v-model="showAddToPlaylistDialog"
      title="添加到歌单"
      width="400px"
      center
    >
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="userPlaylists.length === 0" class="empty-playlists">
        <p class="text-center">你还没有创建歌单</p>
        <div class="text-center">
          <el-button type="primary" @click="createNewPlaylist">创建歌单</el-button>
        </div>
      </div>
      
      <div v-else class="playlist-list">
        <div 
          v-for="playlist in userPlaylists" 
          :key="playlist.id" 
          class="playlist-item"
          @click="addToUserPlaylist(playlist.id)"
        >
          <img :src="getProcessedImageUrl(playlist.coverImg)" class="playlist-cover">
          <div class="playlist-info">
            <div class="playlist-name">{{ playlist.name }}</div>
            <div class="playlist-count">{{ playlist.songCount || 0 }}首歌曲</div>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 创建歌单对话框 -->
    <CreatePlaylistDialog ref="createPlaylistDialogRef" @created="handlePlaylistCreated" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { API_BASE_URL } from '@/api/config'
import { formatDuration, formatDate } from '@/utils/formatters'
import { usePlayerStore } from '@/stores/playerStore'
import { useUserStore } from '@/stores/userStore'
import { CaretRight, Plus, Star, Document } from '@element-plus/icons-vue'
import { getUserPlaylists, addSongToUserPlaylist } from '@/api/userPlaylist'
import { addFavoriteSong, removeFavoriteSong, checkFavoriteSong } from '@/api/favoriteSong'
import CreatePlaylistDialog from '@/components/playlist/CreatePlaylistDialog.vue'

const playerStore = usePlayerStore()
const userStore = useUserStore()
const songs = ref([])
const loading = ref(true)
const error = ref(null)
const defaultCover = 'https://ts1.tc.mm.bing.net/th/id/OIP-C.Qwe67XJJh0t8Gxl-MhzMGwHaHa?w=200&h=211&c=8&rs=1&qlt=90&o=6&dpr=1.3&pid=3.1&rm=2'

// 右键菜单相关状态
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const selectedSong = ref(null)
const isFavorite = ref(false)
const showPlaylistsList = ref(false)
const userPlaylists = ref([])

// 添加歌单对话框
const showAddToPlaylistDialog = ref(false)
const createPlaylistDialogRef = ref(null)

// 获取最新音乐
const fetchLatestSongs = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await axios.get(`${API_BASE_URL}/api/songs/latest`)
    if (response.data.code === 200) {
      songs.value = response.data.data
      console.log('获取歌曲列表:', songs.value)
      // 检查第一首歌时长
      if (songs.value.length > 0) {
        console.log('第一首歌时长:', songs.value[0].duration, '类型:', typeof songs.value[0].duration)
      }
    } else {
      error.value = response.data.message || '获取最新音乐失败'
    }
  } catch (err) {
    console.error('获取最新音乐失败:', err)
    error.value = '网络错误，请稍后再试'
  } finally {
    loading.value = false
  }
}

// 处理右键点击事件
const handleRightClick = (row, column, event) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  event.preventDefault()
  // 设置选中的歌曲
  selectedSong.value = row
  // 设置菜单位置
  contextMenuPosition.value = {
    x: event.clientX,
    y: event.clientY
  }
  
  // 检查歌曲是否已收藏
  checkIsFavorite(row.id)
  
  // 显示菜单
  contextMenuVisible.value = true
  // 默认隐藏歌单列表
  showPlaylistsList.value = false
}

// 检查歌曲是否已收藏
const checkIsFavorite = async (songId) => {
  try {
    const response = await checkFavoriteSong(songId)
    isFavorite.value = response.data
  } catch (error) {
    console.error('检查收藏状态失败:', error)
    isFavorite.value = false
  }
}

// 处理收藏/取消收藏
const handleFavorite = async () => {
  if (!selectedSong.value) return
  
  try {
    if (isFavorite.value) {
      // 取消收藏
      const response = await removeFavoriteSong(selectedSong.value.id)
      if (response.code === 200) {
        ElMessage.success('已取消收藏')
        isFavorite.value = false
      }
    } else {
      // 添加收藏
      const response = await addFavoriteSong(selectedSong.value.id)
      if (response.code === 200) {
        ElMessage.success('已添加到我的收藏')
        isFavorite.value = true
      }
    }
  } catch (err) {
    console.error('收藏操作失败:', err)
    ElMessage.error('操作失败，请重试')
  } finally {
    contextMenuVisible.value = false
  }
}

// 显示添加到歌单菜单
const showAddToPlaylistsMenu = async () => {
  showPlaylistsList.value = true
  
  // 如果还没有加载歌单列表，则加载
  if (userPlaylists.value.length === 0) {
    try {
      const response = await getUserPlaylists()
      if (response.data.code === 200) {
        userPlaylists.value = response.data.data || []
      }
    } catch (error) {
      console.error('获取歌单列表失败:', error)
      ElMessage.error('获取歌单列表失败')
    }
  }
}

// 添加到用户歌单
const addToUserPlaylist = async (playlistId) => {
  if (!selectedSong.value) return
  
  const loading = ElLoading.service({
    lock: true,
    text: '添加中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    const response = await addSongToUserPlaylist(playlistId, selectedSong.value.id)
    if (response.data.code === 200) {
      ElMessage.success('已添加到歌单')
    } else {
      ElMessage.error(response.data.msg || '添加失败')
    }
  } catch (error) {
    console.error('添加歌曲到歌单失败:', error)
    ElMessage.error('添加失败')
  } finally {
    loading.close()
    contextMenuVisible.value = false
  }
}

// 点击页面其他地方关闭菜单
const handleClickOutside = () => {
  contextMenuVisible.value = false
  showPlaylistsList.value = false
}

// 播放歌曲
const playSong = async (song) => {
  console.log('播放歌曲:', song.name, '时长:', song.duration, '类型:', typeof song.duration)
  
  // 确保音频URL格式正确
  let audioUrl = song.fileUrl
  if (!audioUrl) {
    audioUrl = `public/music/${song.name}.mp3`
  }
  
  // 处理封面URL
  const songWithProcessedUrls = {
    ...song,
    cover: getProcessedImageUrl(song.coverImg),
    fileUrl: audioUrl.startsWith('http') ? audioUrl : `${API_BASE_URL}/${audioUrl.replace(/^\/+/, '')}`
  }
  
  console.log('播放前处理:', songWithProcessedUrls)
  playerStore.playSong(songWithProcessedUrls)
  
  // 记录最近播放
  const token = localStorage.getItem('token')
  if (token) {
    try {
      await axios.post(`${API_BASE_URL}/api/recent/add`, null, {
        params: { songId: song.id },
        headers: { token }
      })
      console.log('记录最近播放成功')
    } catch (err) {
      console.error('记录最近播放失败:', err)
    }
  }
  
  // 增加播放次数
  try {
    const response = await axios.post(`${API_BASE_URL}/api/songs/${song.id}/play`)
    if (response.data.code === 200 && response.data.data) {
      // 更新本地歌曲的播放次数
      const updatedSong = response.data.data
      const songIndex = songs.value.findIndex(s => s.id === song.id)
      if (songIndex !== -1) {
        songs.value[songIndex].playCount = updatedSong.playCount
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
    audioUrl = `public/music/${song.name}.mp3`
  }
  
  // 处理封面URL
  const songWithProcessedUrls = {
    ...song,
    cover: getProcessedImageUrl(song.coverImg),
    fileUrl: audioUrl.startsWith('http') ? audioUrl : `${API_BASE_URL}/${audioUrl.replace(/^\/+/, '')}`
  }
  
  playerStore.addToPlaylist(songWithProcessedUrls)
  ElMessage.success(`已添加到播放队列: ${song.name}`)
}

// 添加到歌单
const addToPlaylist = (song) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 设置当前选中的歌曲
  selectedSong.value = song
  
  // 显示添加到歌单对话框
  showAddToPlaylistDialog.value = true
  loadUserPlaylists()
}

// 加载用户歌单
const loadUserPlaylists = async () => {
  loading.value = true
  
  try {
    const response = await getUserPlaylists()
    if (response.data.code === 200) {
      userPlaylists.value = response.data.data || []
    } else {
      ElMessage.error(response.data.msg || '获取歌单失败')
    }
  } catch (error) {
    console.error('获取歌单失败:', error)
    ElMessage.error('获取歌单失败')
  } finally {
    loading.value = false
  }
}

// 创建新歌单
const createNewPlaylist = () => {
  showAddToPlaylistDialog.value = false
  createPlaylistDialogRef.value?.show()
}

// 处理歌单创建成功
const handlePlaylistCreated = () => {
  loadUserPlaylists()
  showAddToPlaylistDialog.value = true
}

// 处理图片URL
const getProcessedImageUrl = (url) => {
  if (!url) {
    console.log('封面图片URL为空，使用默认封面')
    return defaultCover
  }

  // 如果URL不是以http开头，添加API基础URL
  if (!url.startsWith('http')) {
    // 确保路径前面有/public/image/
    if (!url.includes('/public/image/')) {
      url = 'public/image/' + url
    }
    // 移除开头的斜杠以避免双斜杠问题
    url = url.replace(/^\/+/, '')
    const processedUrl = `${API_BASE_URL}/${url}`
    // console.log('处理后的封面URL:', processedUrl)
    return processedUrl
  }
  
  console.log('封面URL已是完整路径:', url)
  return url
}

// 处理图片加载错误
const logImageError = (song) => {
  console.error(`图片加载失败，歌曲: ${song.name}, 原始URL: ${song.coverImg}`)
}

// 监听全局点击事件，用于关闭右键菜单
onMounted(() => {
  fetchLatestSongs()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.latest-music-container {
  padding: 20px;
  position: relative;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #333;
}

.loading-container {
  padding: 20px 0;
}

.error-message {
  color: #f56c6c;
  font-size: 16px;
  text-align: center;
  padding: 40px 0;
}

.song-info {
  display: flex;
  align-items: center;
}

.cover-img {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  margin-right: 12px;
}

.song-details {
  display: flex;
  flex-direction: column;
}

.song-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.song-artist {
  font-size: 12px;
  color: #909399;
}

/* 右键菜单样式 */
.context-menu {
  position: fixed;
  z-index: 1000;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-width: 160px;
  padding: 5px 0;
}

.context-menu-item {
  padding: 8px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

.context-menu-item:hover {
  background-color: #f5f7fa;
}

.context-menu-item .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.context-menu-submenu {
  position: absolute;
  left: 100%;
  top: 0;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-width: 160px;
  padding: 5px 0;
}

.context-menu-item.disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

/* 添加到歌单对话框样式 */
.empty-playlists {
  padding: 20px 0;
  text-align: center;
}

.text-center {
  text-align: center;
  margin: 15px 0;
}

.playlist-list {
  max-height: 350px;
  overflow-y: auto;
}

.playlist-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.playlist-item:hover {
  background-color: #f5f7fa;
}

.playlist-cover {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
  margin-right: 12px;
}

.playlist-info {
  flex: 1;
}

.playlist-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.playlist-count {
  font-size: 12px;
  color: #909399;
}
</style> 
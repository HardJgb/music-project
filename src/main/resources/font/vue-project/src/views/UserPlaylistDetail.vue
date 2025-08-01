<template>
  <div class="playlist-detail">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 歌单详情 -->
    <template v-else-if="playlist">
      <div class="playlist-header">
        <div class="playlist-cover">
          <img :src="getPlaylistCover()" :alt="playlist.name">
        </div>
        <div class="playlist-info">
          <div class="playlist-title">
            <h1>{{ playlist.name }}</h1>
            <span v-if="playlist.isPrivate" class="private-tag">私密</span>
          </div>
          <div class="playlist-meta">
            <div class="meta-item">
              <span class="creator-name">{{ userName }}</span>
            </div>
            <div class="meta-item">
              <span class="create-time">创建于 {{ formatDate(playlist.createTime) }}</span>
            </div>
          </div>
          <div class="playlist-desc" v-if="playlist.description">
            <p>{{ playlist.description }}</p>
          </div>
          <div class="playlist-actions">
            <el-button type="primary" @click="playAll" :disabled="!songs.length">
              <el-icon><CaretRight /></el-icon> 播放全部
            </el-button>
            <el-button @click="showEditPlaylistDialog" v-if="isOwner">
              <el-icon><Edit /></el-icon> 编辑歌单
            </el-button>
            <el-button type="danger" @click="deletePlaylist" v-if="isOwner">
              <el-icon><Delete /></el-icon> 删除歌单
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 歌曲列表 -->
      <div class="songs-container">
        <div class="songs-header">
          <h2>歌曲列表 <span class="song-count" v-if="songs.length">{{ songs.length }}首歌</span></h2>
          <div class="songs-header-actions" v-if="isOwner && songs.length > 0">
            <el-button type="danger" size="small" @click="batchRemoveSongs" :disabled="selectedSongs.length === 0">
              <el-icon><Delete /></el-icon> 删除选中歌曲
            </el-button>
          </div>
        </div>
        
        <div v-if="songs.length > 0" class="song-list">
          <el-table 
            :data="songs" 
            style="width: 100%" 
            @row-dblclick="playSong"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" v-if="isOwner" />
            <el-table-column type="index" width="50" />
            <el-table-column label="歌曲" min-width="300">
              <template #default="scope">
                <div class="song-info">
                  <img :src="getSongCover(scope.row)" class="song-cover" :alt="scope.row.name">
                  <div class="song-title-artist">
                    <span class="song-title">{{ scope.row.name }}</span>
                    <span class="song-artist">{{ scope.row.artist }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="时长" width="120">
              <template #default="scope">
                <span>{{ formatDuration(scope.row.duration) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <div class="song-actions">
                  <el-button type="text" @click.stop="playSong(scope.row)">
                    <el-icon><VideoPlay /></el-icon>
                  </el-button>
                  <el-button type="text" @click.stop="removeSongFromPlaylist(scope.row)" v-if="isOwner">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div v-else class="empty-songs">
          <el-empty description="暂无歌曲，可以从其他页面添加歌曲到此歌单" />
        </div>
      </div>
    </template>
    
    <!-- 歌单不存在 -->
    <div v-else class="not-found">
      <el-empty description="歌单不存在或已被删除" />
      <el-button type="primary" @click="$router.push('/my-playlists')">返回我的歌单</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { CaretRight, Edit, VideoPlay, Delete } from '@element-plus/icons-vue'
import { getUserPlaylistDetail, removeSongFromUserPlaylist, deleteUserPlaylist } from '@/api/userPlaylist'
import { API_BASE_URL } from '@/api/config'
import { usePlayerStore } from '@/stores/playerStore'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()

const playlist = ref(null)
const songs = ref([])
const loading = ref(true)
const selectedSongs = ref([])

const isOwner = computed(() => {
  return playlist.value && playlist.value.userId === userStore.userId
})

const userName = computed(() => {
  return userStore.username
})

// 获取歌单详情
const fetchPlaylistDetail = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  const playlistId = route.params.id
  if (!playlistId) {
    ElMessage.error('参数错误')
    router.push('/my-playlists')
    return
  }
  
  loading.value = true
  try {
    const response = await getUserPlaylistDetail(playlistId)
    if (response.data.code === 200) {
      const data = response.data.data || {}
      playlist.value = data.playlist || null
      songs.value = data.songs || []
    } else {
      ElMessage.error(response.data.msg || '获取歌单详情失败')
      playlist.value = null
    }
  } catch (error) {
    console.error('获取歌单详情失败:', error)
    ElMessage.error('获取歌单详情失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
    playlist.value = null
  } finally {
    loading.value = false
  }
}

// 播放歌曲
const playSong = (song) => {
  // 处理歌曲对象，确保封面URL正确
  const processedSong = {
    ...song,
    cover: getSongCover(song),
    coverImg: getSongCover(song)
  }
  playerStore.playSong(processedSong)
}

// 播放全部
const playAll = () => {
  if (songs.value.length === 0) {
    ElMessage.warning('当前歌单没有歌曲')
    return
  }
  
  // 处理所有歌曲的封面URL
  const processedSongs = songs.value.map(song => ({
    ...song,
    cover: getSongCover(song),
    coverImg: getSongCover(song)
  }))
  
  playerStore.playList(processedSongs)
}

// 从歌单中移除歌曲
const removeSongFromPlaylist = (song) => {
  ElMessageBox.confirm(
    `确定要从歌单中移除歌曲"${song.name}"吗？`,
    '移除歌曲',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await removeSongFromUserPlaylist(playlist.value.id, song.id)
      if (response.data.code === 200) {
        ElMessage.success('移除成功')
        // 从列表中移除
        songs.value = songs.value.filter(s => s.id !== song.id)
      } else {
        ElMessage.error(response.data.msg || '移除失败')
      }
    } catch (error) {
      console.error('移除歌曲失败:', error)
      ElMessage.error('移除失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
    }
  }).catch(() => {
    // 用户取消操作，不做任何处理
  })
}

// 显示编辑歌单对话框
const showEditPlaylistDialog = () => {
  ElMessage.info('编辑歌单功能将在后续版本中提供')
}

// 获取歌单封面
const getPlaylistCover = () => {
  if (!playlist.value || !playlist.value.coverImg) {
    return `${API_BASE_URL}/public/image/default-cover.jpg`
  }
  
  if (playlist.value.coverImg.startsWith('http')) {
    return playlist.value.coverImg
  }
  
  return `${API_BASE_URL}${playlist.value.coverImg.startsWith('/') ? '' : '/'}${playlist.value.coverImg}`
}

// 获取歌曲封面
const getSongCover = (song) => {
  if (!song.coverImg && !song.cover) {
    return `${API_BASE_URL}/public/image/default-cover.jpg`
  }
  
  const coverUrl = song.coverImg || song.cover
  
  if (coverUrl.startsWith('http')) {
    return coverUrl
  }
  
  return `${API_BASE_URL}${coverUrl.startsWith('/') ? '' : '/'}${coverUrl}`
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date)) return ''
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = Math.floor(seconds % 60)
  
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 删除整个歌单
const deletePlaylist = () => {
  ElMessageBox.confirm(
    `确定要删除歌单"${playlist.value.name}"吗？删除后无法恢复。`,
    '删除歌单',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      const response = await deleteUserPlaylist(playlist.value.id)
      if (response.data.code === 200) {
        ElMessage.success('歌单删除成功')
        // 返回歌单列表页
        router.push('/my-playlists')
      } else {
        ElMessage.error(response.data.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除歌单失败:', error)
      ElMessage.error('删除失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
    }
  }).catch(() => {
    // 用户取消操作，不做任何处理
  })
}

// 处理表格选中行变化
const handleSelectionChange = (selection) => {
  selectedSongs.value = selection
}

// 批量删除歌曲
const batchRemoveSongs = () => {
  if (selectedSongs.value.length === 0) {
    ElMessage.warning('请先选择要删除的歌曲')
    return
  }
  
  ElMessageBox.confirm(
    `确定要从歌单中移除 ${selectedSongs.value.length} 首选中的歌曲吗？`,
    '批量移除歌曲',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    let successCount = 0
    let failCount = 0
    
    // 显示加载中状态
    const loadingInstance = ElLoading.service({
      text: '正在删除...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    try {
      // 依次删除选中的歌曲
      for (const song of selectedSongs.value) {
        try {
          const response = await removeSongFromUserPlaylist(playlist.value.id, song.id)
          if (response.data.code === 200) {
            successCount++
          } else {
            failCount++
          }
        } catch (error) {
          failCount++
          console.error(`删除歌曲 ${song.name} 失败:`, error)
        }
      }
      
      // 更新歌曲列表
      if (successCount > 0) {
        const selectedIds = selectedSongs.value.map(s => s.id)
        songs.value = songs.value.filter(s => !selectedIds.includes(s.id))
        selectedSongs.value = []
        
        ElMessage.success(`成功删除 ${successCount} 首歌曲${failCount > 0 ? `，${failCount} 首删除失败` : ''}`)
      } else if (failCount > 0) {
        ElMessage.error(`全部删除失败，请重试`)
      }
    } catch (error) {
      console.error('批量删除歌曲失败:', error)
      ElMessage.error('批量删除失败: ' + (error.message || '未知错误'))
    } finally {
      loadingInstance.close()
    }
  }).catch(() => {
    // 用户取消操作，不做任何处理
  })
}

onMounted(() => {
  fetchPlaylistDetail()
})
</script>

<style scoped>
.playlist-detail {
  padding: 20px;
}

.loading-container {
  padding: 20px;
}

.playlist-header {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.playlist-cover {
  width: 200px;
  height: 200px;
  min-width: 200px;
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
}

.playlist-title {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.playlist-title h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.private-tag {
  display: inline-block;
  margin-left: 10px;
  padding: 2px 8px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 14px;
  color: #666;
}

.playlist-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.creator-name {
  color: #333;
}

.playlist-desc {
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.playlist-actions {
  display: flex;
  gap: 10px;
  margin-top: auto;
}

.songs-container {
  margin-top: 30px;
}

.songs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.songs-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.song-count {
  font-size: 14px;
  color: #666;
  margin-left: 8px;
  font-weight: normal;
}

.songs-header-actions {
  display: flex;
  gap: 8px;
}

.song-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.song-cover {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

.song-title-artist {
  display: flex;
  flex-direction: column;
}

.song-title {
  font-size: 14px;
  color: #333;
}

.song-artist {
  font-size: 12px;
  color: #666;
}

.song-actions {
  display: flex;
  gap: 5px;
}

.empty-songs, .not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  gap: 20px;
}

:deep(.el-table) {
  --el-table-header-bg-color: #f5f7fa;
  --el-table-row-hover-bg-color: #f0f0f0;
}

:deep(.el-table .cell) {
  padding: 8px 12px;
}
</style> 
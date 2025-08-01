<template>
  <div class="song-item" :class="{ 'active': active, 'dragging': isDragging }" @dblclick="playThisSong">
    <div class="song-index">
      <span v-if="!isPlaying">{{ displayIndex }}</span>
      <el-icon v-else class="playing-icon" color="#ec4141"><VideoPlay /></el-icon>
    </div>
    
    <div class="song-info">
      <div class="song-title-artist">
        <router-link :to="`/song/${song.id}`" class="song-title">{{ song.name }}</router-link>
        <div class="song-artist">
          <router-link 
            v-if="song.artist" 
            :to="`/artist/${encodeURIComponent(song.artist)}`"
            class="artist-link"
          >
            {{ song.artist }}
          </router-link>
          <span v-else>未知歌手</span>
        </div>
      </div>
    </div>
    
    <div class="song-album" v-if="showAlbum">{{ song.album || '未知专辑' }}</div>
    
    <div class="song-duration">{{ formatTime(song.duration) }}</div>
    
    <div class="song-actions">
      <span class="action-icon" @click="addToFavorite" v-if="!song.isFavorite">
        <el-icon><Star /></el-icon>
      </span>
      <span class="action-icon favorited" @click="removeFromFavorite" v-else>
        <el-icon><StarFilled /></el-icon>
      </span>
      
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="action-icon">
          <el-icon><MoreFilled /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="play">播放</el-dropdown-item>
            <el-dropdown-item command="nextPlay">下一首播放</el-dropdown-item>
            <el-dropdown-item divided command="addToPlaylist">添加到歌单</el-dropdown-item>
            <el-dropdown-item command="download">下载</el-dropdown-item>
            <el-dropdown-item v-if="canDelete" divided command="delete">从列表删除</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, inject } from 'vue'
import { VideoPlay, Star, StarFilled, MoreFilled } from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores/playerStore'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { API_BASE_URL } from '@/api/config'

const props = defineProps({
  song: {
    type: Object,
    required: true
  },
  index: {
    type: Number,
    required: true
  },
  showAlbum: {
    type: Boolean,
    default: true
  },
  active: {
    type: Boolean,
    default: false
  },
  isDragging: {
    type: Boolean,
    default: false
  },
  canDelete: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['delete', 'add-to-playlist', 'update:song'])

const playerStore = usePlayerStore()
const openPlaylistSelector = inject('openPlaylistSelector', () => {})

// 计算属性
const isPlaying = computed(() => {
  return props.active && playerStore.playing
})

const displayIndex = computed(() => {
  return props.index < 9 ? '0' + (props.index + 1) : (props.index + 1)
})

// 方法
const formatTime = (seconds) => {
  if (!seconds) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const playThisSong = () => {
  playerStore.playSong(props.song)
}

const addToFavorite = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录')
      return
    }
    
    const response = await axios.post(`${API_BASE_URL}/api/user/favorite/add/${props.song.id}`, null, {
      headers: { token }
    })
    
    if (response.data.code === 200) {
      emit('update:song', { ...props.song, isFavorite: true })
      ElMessage.success('已添加到我喜欢的音乐')
    } else {
      ElMessage.error(response.data.message || '收藏失败')
    }
  } catch (error) {
    console.error('添加收藏失败:', error)
    ElMessage.error('网络错误，请稍后再试')
  }
}

const removeFromFavorite = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录')
      return
    }
    
    const response = await axios.post(`${API_BASE_URL}/api/user/favorite/remove/${props.song.id}`, null, {
      headers: { token }
    })
    
    if (response.data.code === 200) {
      emit('update:song', { ...props.song, isFavorite: false })
      ElMessage.success('已从我喜欢的音乐中移除')
    } else {
      ElMessage.error(response.data.message || '取消收藏失败')
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('网络错误，请稍后再试')
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'play':
      playThisSong()
      break
    case 'nextPlay':
      playerStore.addToNext(props.song)
      ElMessage.success('已添加到下一首播放')
      break
    case 'addToPlaylist':
      openPlaylistSelector(props.song)
      break
    case 'download':
      downloadSong()
      break
    case 'delete':
      emit('delete', props.song)
      break
  }
}

const downloadSong = () => {
  if (!props.song.url) {
    ElMessage.error('无法下载，歌曲链接不存在')
    return
  }
  
  let url = props.song.url
  if (!url.startsWith('http')) {
    url = `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
  }
  
  const a = document.createElement('a')
  a.href = url
  a.download = `${props.song.name} - ${props.song.artist || '未知歌手'}.mp3`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}
</script>

<style scoped>
.song-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-radius: 4px;
  color: #333;
  transition: all 0.2s ease;
}

.song-item:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.song-item.active {
  background-color: rgba(236, 65, 65, 0.1);
}

.song-item.dragging {
  opacity: 0.5;
}

.song-index {
  width: 50px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.playing-icon {
  animation: pulsate 1.5s infinite;
}

@keyframes pulsate {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.song-info {
  flex: 3;
  min-width: 0;
}

.song-title-artist {
  display: flex;
  flex-direction: column;
}

.song-title {
  color: #333;
  text-decoration: none;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s;
}

.song-title:hover {
  color: #ec4141;
}

.song-artist {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 3px;
}

.artist-link {
  color: #666;
  text-decoration: none;
  transition: color 0.2s;
}

.artist-link:hover {
  color: #ec4141;
}

.song-album {
  flex: 2;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-duration {
  width: 80px;
  text-align: center;
  font-size: 14px;
  color: #999;
}

.song-actions {
  width: 80px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}

.song-item:hover .song-actions {
  opacity: 1;
}

.action-icon {
  cursor: pointer;
  color: #999;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.action-icon:hover {
  color: #ec4141;
  transform: scale(1.1);
}

.action-icon.favorited {
  color: #ec4141;
}
</style> 
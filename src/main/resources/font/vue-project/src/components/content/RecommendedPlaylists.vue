<template>
  <div class="recommended-playlists">
    <div class="section-header">
      <h2>推荐歌单</h2>
      <router-link to="/playlists" class="more-link">更多 ></router-link>
    </div>
    
    <div class="playlist-grid">
      <div 
        v-for="playlist in playlists" 
        :key="playlist.id" 
        class="playlist-item" 
        @click="goToPlaylist(playlist.id)"
        @dblclick="playFirstSong(playlist.id)"
      >
        <div class="playlist-cover">
          <img :src="getImageUrl(playlist.coverImg)" :alt="playlist.name">
          <div class="play-count">
            <i class="el-icon-headset"></i>
            {{ formatPlayCount(playlist.playCount) }}
          </div>
        </div>
        <div class="playlist-name">{{ playlist.name }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { PLAYLIST_API, getImageUrl } from '@/api/config'
import { useRouter } from 'vue-router'
import { usePlayerStore } from '@/stores/playerStore'

const playlists = ref([])
const loading = ref(false)
const error = ref(null)
const router = useRouter()
const playerStore = usePlayerStore()

// 获取推荐歌单数据
const fetchRecommendedPlaylists = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await axios.get(PLAYLIST_API.getRecommendedPlaylists)
    if (response.data.code === 200) {
      playlists.value = response.data.data
      console.log('获取到的推荐歌单:', playlists.value)
    } else {
      error.value = response.data.message || '获取推荐歌单失败'
    }
  } catch (err) {
    console.error('获取推荐歌单出错:', err)
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

// 歌单点击跳转到详情页
const goToPlaylist = (id) => {
  router.push(`/playlist/${id}`)
}

// 双击时播放歌单的第一首歌
const playFirstSong = async (id) => {
  try {
    const response = await axios.get(PLAYLIST_API.getPlaylistById(id))
    if (response.data.code === 200 && response.data.data.songs && response.data.data.songs.length > 0) {
      const firstSong = response.data.data.songs[0]
      playerStore.playSong({
        ...firstSong,
        cover: firstSong.coverImg ? getImageUrl(firstSong.coverImg) : null
      })
    }
  } catch (err) {
    console.error('获取歌单歌曲失败:', err)
  }
}

onMounted(() => {
  fetchRecommendedPlaylists()
})
</script>

<style scoped>
.recommended-playlists {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.more-link {
  color: #666;
  font-size: 12px;
  text-decoration: none;
}

.more-link:hover {
  color: #ec4141;
}

.playlist-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.playlist-item {
  cursor: pointer;
}

.playlist-cover {
  position: relative;
  width: 100%;
  padding-bottom: 100%; /* 保持宽高比为1:1 */
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.playlist-cover img {
  position: absolute;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.playlist-item:hover .playlist-cover img {
  transform: scale(1.05);
}

.play-count {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
}

.playlist-name {
  font-size: 14px;
  margin-top: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style> 
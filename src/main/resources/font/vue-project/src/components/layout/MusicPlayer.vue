<template>
  <div class="music-player" :class="{ 'expanded': expanded }">
    <!-- 音频元素 -->
    <audio 
      ref="audioPlayer"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="onLoadedMetadata"
      @ended="onEnded"
    ></audio>
    
    <!-- 进度条 -->
    <div class="player-progress" @click="onProgressBarClick">
      <el-slider 
        v-model="progress" 
        :show-tooltip="false" 
        class="progress-slider"
        @change="onProgressChange"
        @input="onProgressInput"
      />
    </div>
    
    <div class="player-content">
      <!-- 歌曲信息 -->
      <div class="song-info" v-if="currentSong">
        <div class="song-cover" @click="openSongDetail">
          <img :src="currentSong.cover || currentSong.coverImg || defaultCover" :alt="currentSong.name">
          <div class="cover-overlay">
            <el-icon><ZoomIn /></el-icon>
          </div>
        </div>
        <div class="song-details">
          <div class="song-name">{{ currentSong.name }}</div>
          <div class="song-artist">{{ currentSong.artist }}</div>
        </div>
      </div>
      <div class="song-info placeholder" v-else>
        <div class="song-cover placeholder">
          <div class="music-icon">
            <el-icon><Headset /></el-icon>
          </div>
        </div>
        <div class="song-details">
          <div class="song-name">选择歌曲播放</div>
          <div class="song-artist">享受音乐</div>
        </div>
      </div>
      
      <!-- 播放控制 -->
      <div class="player-controls">
        <button class="control-btn" @click="prevSong">
          <el-icon><Back /></el-icon>
        </button>
        
        <button class="control-btn play-btn" @click="togglePlay">
          <el-icon v-if="playing"><VideoPause /></el-icon>
          <el-icon v-else><VideoPlay /></el-icon>
        </button>
        
        <button class="control-btn" @click="nextSong">
          <el-icon><Right /></el-icon>
        </button>
      </div>
      
      <!-- 音量和时间 -->
      <div class="player-options">
        <span class="time-display">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>
        
        <div class="volume-control">
          <el-icon><Mute v-if="volume === 0" /><Headset v-else /></el-icon>
          <el-slider 
            v-model="volume" 
            :show-tooltip="false" 
            :min="0" 
            :max="100"
            class="volume-slider"
            @change="onVolumeChange" 
          />
        </div>
        
        <!-- 评论按钮 -->
        <div class="comment-btn" @click="goToSongDetail" v-if="currentSong">
          <el-tooltip content="评论" placement="top" :hide-after="1500">
            <el-icon><ChatDotRound /></el-icon>
          </el-tooltip>
        </div>
        
        <div class="player-mode" @click="toggleExpand">
          <el-tooltip content="播放列表" placement="top" :hide-after="1500">
            <el-icon><Menu /></el-icon>
          </el-tooltip>
        </div>
      </div>
    </div>
    
    <!-- 播放列表 -->
    <div class="player-expanded" v-show="expanded">
      <div class="player-playlist">
        <div class="playlist-header">
          <h3>播放列表 ({{ playlist.length }})</h3>
          <button class="clear-btn" @click="clearPlaylist">
            <el-icon><Delete /></el-icon> 清空
          </button>
        </div>
        
        <div class="playlist-content">
          <div 
            v-for="(song, index) in playlist" 
            :key="song.id" 
            class="playlist-item"
            :class="{ 'active': currentSong?.id === song.id }"
            @click="playSongAtIndex(index)"
          >
            <div class="song-info">
              <div class="song-name">{{ song.name }}</div>
              <div class="song-artist">{{ song.artist }}</div>
              <img 
                class="playlist-item-cover" 
                :src="song.cover || song.coverImg || defaultCover" 
                :alt="song.name"
              >
            </div>
            <div class="song-duration">{{ formatTime(song.duration) }}</div>
            <button class="remove-btn" @click.stop="removeSongFromPlaylist(index)">
              <el-icon><Close /></el-icon>
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 歌曲详情页面 -->
    <song-detail-view 
      v-if="showSongDetail" 
      :song="currentSong" 
      @close="closeSongDetail" 
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { usePlayerStore } from '@/stores/playerStore'
import { storeToRefs } from 'pinia'
import { API_BASE_URL } from '@/api/config'
import { useRouter } from 'vue-router'
import { 
  Back, Right, VideoPlay, VideoPause,
  Delete, Close, Headset, Mute, Menu,
  ZoomIn, ChatDotRound
} from '@element-plus/icons-vue'
import SongDetailView from '@/components/song/SongDetailView.vue'
import axios from 'axios'

const router = useRouter()
const playerStore = usePlayerStore()
const audioPlayer = ref(null)
const expanded = ref(false)
const showSongDetail = ref(false)
const progress = ref(0)
const volume = ref(70)
const defaultCover = `${API_BASE_URL}/public/image/default-cover.jpg`

const { 
  currentSong,
  playing,
  currentTime,
  duration,
  playlist
} = storeToRefs(playerStore)

// 监听播放状态变化
watch(() => playerStore.playing, (isPlaying) => {
  console.log('播放状态变更为:', isPlaying)
  if (isPlaying) {
    if (!audioPlayer.value) {
      console.error('找不到audio元素')
      return
    }
    
    if (!playerStore.currentSong?.fileUrl) {
      console.error('当前歌曲无fileUrl')
      return
    }
    
    audioPlayer.value.play()
      .then(() => {
        console.log('播放成功')
        // 增加播放次数
        if (playerStore.currentSong?.id) {
          incrementSongPlayCount(playerStore.currentSong.id)
        }
      })
      .catch(err => console.error('播放失败:', err))
  } else {
    audioPlayer.value?.pause()
  }
})

// 监听当前歌曲变化
watch(() => playerStore.currentSong, async (newSong) => {
  console.log('当前歌曲变更为:', newSong)
  if (newSong) {
    console.log('歌曲时长:', newSong.duration, '类型:', typeof newSong.duration)
    
    // 记录最近播放
    const token = localStorage.getItem('token')
    console.log('当前token:', token)
    if (token && newSong.id) {
      try {
        const response = await axios.post(`${API_BASE_URL}/api/recent/add`, null, {
          params: { songId: newSong.id },
          headers: { token: token }
        })
        console.log('记录最近播放成功:', response.data)
      } catch (err) {
        console.error('记录最近播放失败:', err)
      }
    } else {
      console.log('未记录最近播放，token:', token, 'songId:', newSong.id)
    }
  }
  
  if (newSong && audioPlayer.value) {
    // 处理文件URL路径
    let audioUrl = newSong.fileUrl
    
    // 如果URL不是以http开头，添加API基础URL
    if (audioUrl && !audioUrl.startsWith('http')) {
      audioUrl = `http://localhost:8085${audioUrl.startsWith('/') ? '' : '/'}${audioUrl}`
    }
    
    // 手动设置音频源
    audioPlayer.value.src = audioUrl
    
    // 预加载
    audioPlayer.value.load()
    
    // 当切换歌曲时，如果处于播放状态则直接播放
    if (playerStore.playing) {
      audioPlayer.value.play()
        .then(() => console.log('播放成功'))
        .catch(err => {
          console.error('播放失败:', err)
          // 尝试通过设置src然后播放
          if (audioPlayer.value) {
            console.log('尝试重新加载并播放...')
            audioPlayer.value.src = audioUrl
            audioPlayer.value.load()
            return audioPlayer.value.play()
          }
        })
        .catch(err => console.error('二次尝试播放失败:', err))
    }
  }
}, { immediate: true })

// 打开歌曲详情页面
const openSongDetail = () => {
  if (currentSong.value) {
    showSongDetail.value = true
  }
}

// 关闭歌曲详情页面
const closeSongDetail = () => {
  showSongDetail.value = false
}

// 播放控制方法
const togglePlay = () => {
  playerStore.togglePlay()
  console.log('播放状态切换为:', playerStore.playing)
}

const prevSong = () => {
  playerStore.prevSong()
}

const nextSong = () => {
  playerStore.nextSong()
}

const toggleExpand = () => {
  expanded.value = !expanded.value
}

const onTimeUpdate = () => {
  if (audioPlayer.value) {
    playerStore.updateProgress(audioPlayer.value.currentTime)
    progress.value = (audioPlayer.value.currentTime / audioPlayer.value.duration) * 100 || 0
  }
}

const onLoadedMetadata = () => {
  if (audioPlayer.value) {
    playerStore.updateDuration(audioPlayer.value.duration)
  }
}

const onEnded = () => {
  nextSong()
}

const onProgressChange = (val) => {
  if (audioPlayer.value && audioPlayer.value.duration) {
    const time = (val / 100) * audioPlayer.value.duration
    audioPlayer.value.currentTime = time
    playerStore.updateProgress(time)
  }
}

const onProgressInput = (val) => {
  if (audioPlayer.value && audioPlayer.value.duration) {
    const time = (val / 100) * audioPlayer.value.duration
    audioPlayer.value.currentTime = time
    playerStore.updateProgress(time)
  }
}

const onVolumeChange = (val) => {
  if (audioPlayer.value) {
    audioPlayer.value.volume = val / 100
  }
}

// 播放列表控制
const playSongAtIndex = (index) => {
  if (index >= 0 && index < playlist.value.length) {
    playerStore.playSong(playlist.value[index])
  }
}

const removeSongFromPlaylist = (index) => {
  if (index >= 0 && index < playlist.value.length) {
    const songToRemove = playlist.value[index]
    playlist.value.splice(index, 1)
    
    // 如果移除的是当前播放的歌曲，切换到下一首
    if (currentSong.value?.id === songToRemove.id) {
      if (playlist.value.length > 0) {
        const nextIndex = index % playlist.value.length
        playerStore.playSong(playlist.value[nextIndex])
      } else {
        playerStore.currentSong = null
        playerStore.playing = false
      }
    }
  }
}

const clearPlaylist = () => {
  playlist.value = []
  playerStore.currentSong = null
  playerStore.playing = false
}

// 格式化时间
const formatTime = (seconds) => {
  // console.log('格式化时间输入:', seconds, '类型:', typeof seconds)

  if (!seconds || isNaN(seconds)) {
    console.log('无效时长，返回 00:00')
    return '00:00'
  }
  
  // 检查是否是毫秒格式，如果大于10000，认为是毫秒格式
  if (seconds > 10000) {
    console.log('检测到毫秒格式，转换为秒:', seconds, '->', seconds/1000)
    seconds = seconds / 1000; // 转换为秒
  }
  
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  const result = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  // console.log('格式化后时长:', result)
  return result
}

const onProgressBarClick = (event) => {
  const progressBar = event.currentTarget
  const rect = progressBar.getBoundingClientRect()
  const percentage = (event.clientX - rect.left) / rect.width
  if (audioPlayer.value && audioPlayer.value.duration) {
    const time = percentage * audioPlayer.value.duration
    audioPlayer.value.currentTime = time
    progress.value = percentage * 100
    playerStore.updateProgress(time)
  }
}

// 调用增加播放次数的API
const incrementSongPlayCount = async (songId) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/songs/${songId}/play`)
    console.log('增加播放次数结果:', response.data)
    
    // 更新当前歌曲的播放次数
    if (response.data.code === 200 && response.data.data) {
      const updatedSong = response.data.data
      if (playerStore.currentSong && playerStore.currentSong.id === songId) {
        playerStore.currentSong.playCount = updatedSong.playCount
      }
    }
  } catch (error) {
    console.error('增加播放次数失败:', error)
  }
}

// 跳转到歌曲详情页面
const goToSongDetail = () => {
  if (currentSong.value) {
    router.push(`/song/${currentSong.value.id}#comments`)
  }
}

// 初始化
onMounted(() => {
  console.log('MusicPlayer已挂载, 音频元素:', audioPlayer.value)
  if (audioPlayer.value) {
    audioPlayer.value.volume = volume.value / 100
    
    // 预加载
    audioPlayer.value.preload = 'auto'
  }
})
</script>

<style scoped>
.music-player {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  background: linear-gradient(to right, 
    rgba(26, 26, 26, 0.75), 
    rgba(44, 44, 44, 0.75), 
    rgba(26, 26, 26, 0.75)
  );
  animation: gradientBreathing 8s ease-in-out infinite;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
  box-shadow: 0 -2px 15px rgba(0, 0, 0, 0.2);
  z-index: 100;
  color: #fff;
}

.player-content {
  display: flex;
  align-items: center;
  padding: 0 25px;
  height: 80px;
  position: relative;
}

.player-progress {
  width: 100%;
  padding: 0;
  height: 4px;
  cursor: pointer;
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  z-index: 1;
}

.progress-slider {
  --el-slider-height: 4px !important;
  --el-slider-button-size: 10px !important;
  --el-slider-button-wrapper-size: 14px !important;
  --el-slider-button-wrapper-offset: -5px !important;
  --el-slider-main-bg-color: #ec4141 !important;
  --el-slider-runway-bg-color: rgba(255, 255, 255, 0.1) !important;
  --el-slider-stop-bg-color: #ec4141 !important;
  --el-slider-disabled-color: #ec4141 !important;
  margin: 0;
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  z-index: 0;
}

.progress-slider :deep(.el-slider__runway),
.progress-slider :deep(.el-slider__bar),
.progress-slider :deep(.el-slider__button-wrapper) {
  pointer-events: none;
}

.song-info {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 280px;
  min-width: 280px;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.song-cover {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
}

.song-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.song-cover:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4);
}

.song-cover:hover img {
  transform: scale(1.12);
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
}

.song-cover:hover .cover-overlay {
  opacity: 1;
}

.song-details {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.song-name {
  font-weight: 600;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #fff;
  margin-bottom: 4px;
}

.song-artist {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.player-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.control-btn {
  border: none;
  background: transparent;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.2s;
  border-radius: 50%;
  width: 36px;
  height: 36px;
}

.control-btn:hover {
  color: #ec4141;
  background: rgba(255, 255, 255, 0.05);
  transform: scale(1.05);
}

.play-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(145deg, #ec4141, #d33939);
  padding: 0;
  color: white;
  box-shadow: 0 4px 10px rgba(236, 65, 65, 0.3);
  transform: scale(1);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-btn:hover {
  background: linear-gradient(145deg, #f04545, #e13d3d);
  transform: scale(1.08);
  box-shadow: 0 6px 15px rgba(236, 65, 65, 0.4);
  color: white;
}

.player-options {
  display: flex;
  align-items: center;
  gap: 20px;
  width: 280px;
  min-width: 280px;
  justify-content: flex-end;
  margin-left: auto;
}

.time-display {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 120px;
  color: rgba(255, 255, 255, 0.7);
}

.volume-control:hover {
  color: #ec4141;
}

.volume-slider {
  --el-slider-height: 4px !important;
  --el-slider-button-size: 12px !important;
  --el-slider-main-bg-color: #ec4141 !important;
  --el-slider-runway-bg-color: rgba(255, 255, 255, 0.1) !important;
  margin: 0;
}

.player-mode, .comment-btn {
  cursor: pointer;
  margin-left: 15px;
  color: #ccc;
  transition: color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.player-mode:hover, .comment-btn:hover {
  color: #ec4141;
}

/* 播放列表展开部分 */
.player-expanded {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, 
    rgba(26, 26, 26, 0.75) 0%,
    rgba(30, 30, 30, 0.75) 50%,
    rgba(26, 26, 26, 0.75) 100%
  );
  animation: gradientBreathing 8s ease-in-out infinite;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.2);
  height: 350px;
  z-index: 99;
  color: #fff;
  backdrop-filter: blur(10px);
}

.player-playlist {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.playlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 25px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.playlist-header h3 {
  font-size: 16px;
  margin: 0;
  font-weight: 600;
}

.clear-btn {
  border: none;
  background: transparent;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 4px;
  padding: 6px 10px;
  transition: all 0.2s;
}

.clear-btn:hover {
  color: #ec4141;
  background: rgba(255, 255, 255, 0.05);
}

.playlist-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
}

.playlist-content::-webkit-scrollbar {
  width: 6px;
}

.playlist-content::-webkit-scrollbar-track {
  background: transparent;
}

.playlist-content::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.playlist-item {
  display: flex;
  align-items: center;
  padding: 12px 25px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.2s;
}

.playlist-item:hover {
  background-color: rgba(255, 255, 255, 0.06);
}

.playlist-item.active {
  background: linear-gradient(to right,
    rgba(236, 65, 65, 0.12),
    rgba(236, 65, 65, 0.08)
  );
  border-left: 3px solid #ec4141;
}

.playlist-item .song-info {
  flex: 1;
  width: auto;
}

.playlist-item .song-duration {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  margin-right: 15px;
  font-variant-numeric: tabular-nums;
}

.remove-btn {
  opacity: 0;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  transition: all 0.2s;
}

.playlist-item:hover .remove-btn {
  opacity: 1;
}

.remove-btn:hover {
  color: #ec4141;
  background: rgba(255, 255, 255, 0.05);
}

/* 占位符样式 */
.song-info.placeholder {
  opacity: 0.7;
}

.song-cover.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #2a2a2a, #222);
}

.music-icon {
  font-size: 24px;
  color: rgba(255, 255, 255, 0.2);
}

.playlist-item-cover {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  margin-right: 15px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

@keyframes gradientBreathing {
  0% {
    background-color: rgba(26, 26, 26, 0.75);
  }
  50% {
    background-color: rgba(35, 35, 35, 0.75);
  }
  100% {
    background-color: rgba(26, 26, 26, 0.75);
  }
}
</style> 
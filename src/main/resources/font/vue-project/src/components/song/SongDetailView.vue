<template>
  <div class="song-detail-view" @click.self="close">
    <div class="song-detail-container">
      <div class="song-detail-content">
        <!-- Header with title and close button -->
        <div class="song-detail-header">
          <h1 class="song-title">{{ song.name }}</h1>
          <button class="close-btn" @click="close">
            <el-icon><Close /></el-icon>
          </button>
        </div>
        
        <!-- Main content -->
        <div class="song-detail-main">
          <!-- Left side: Record player -->
          <div class="record-player-container">
            <div class="record-player">
              <div class="vinyl-disc" :class="{ 'paused': !playing }">
                <div class="vinyl-label">
                  <img 
                    :src="getProcessedImageUrl(song.cover)" 
                    :alt="song.name" 
                    class="song-cover-img"
                  >
                </div>
              </div>
              <div class="record-player-arm" :class="{ 'paused': !playing }">
                <div class="stylus"></div>
              </div>
            </div>
            
            <div class="song-meta">
              <div class="song-artist">
                <router-link 
                  v-if="song.artist"
                  :to="`/artist/${encodeURIComponent(song.artist)}`"
                  class="artist-link"
                >
                  歌手：{{ song.artist }}
                </router-link>
                <span v-else>未知歌手</span>
              </div>
              
              <div class="song-album-info">
                <span class="detail-label">专辑：</span>
                <span class="detail-value">{{ song.album || '未知专辑' }}</span>
              </div>
            </div>
          </div>
          
          <!-- Right side: Lyrics -->
          <div class="lyrics-container">
            <h3 class="lyrics-title">歌词</h3>
            <div class="lyrics-content" v-if="lyrics.length">
              <p v-for="(line, index) in lyrics" :key="index" class="lyric-line" :class="{ active: line.active }">
                <span v-for="(char, charIndex) in line.characters"
                      :key="charIndex"
                      :class="{ 
                        'char-highlighted': line.active && charIndex <= line.highlightIndex,
                        'char-normal': !line.active || charIndex > line.highlightIndex
                      }">
                  {{ char.char }}
                </span>
              </p>
            </div>
            <div class="no-lyrics" v-else>
              <p>暂无歌词</p>
            </div>
          </div>
        </div>
        
        <!-- Bottom playback controls -->
        <div class="playback-controls">
          <div class="song-info">
            <div class="song-detail-item">
              <span class="detail-label">时长：</span>
              <span class="detail-value">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>
            </div>
            <div class="song-detail-item" v-if="song.playCount">
              <span class="detail-label">播放次数：</span>
              <span class="detail-value">{{ song.playCount }}</span>
            </div>
          </div>
          
          <div class="controls-wrapper">
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
          
          <div class="volume-controls">
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
        </div>
        
        <!-- Progress bar -->
        <div class="progress-container">
          <el-slider 
            v-model="progress" 
            :show-tooltip="false" 
            class="progress-slider"
            @change="onProgressChange" 
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { 
  Close, VideoPlay, VideoPause, Back, 
  Right, Headset, Mute 
} from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores/playerStore'
import { storeToRefs } from 'pinia'
import { API_BASE_URL } from '@/api/config'
import axios from 'axios'

const props = defineProps({
  song: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close'])

const lyrics = ref([])
const playerStore = usePlayerStore()
const { 
  playing, currentTime, duration 
} = storeToRefs(playerStore)

// Local player state
const progress = ref(0)
const volume = ref(70)

// 处理图片URL
const getProcessedImageUrl = (url) => {
  if (!url) {
    return `${API_BASE_URL}/public/image/default-cover.jpg`
  }

  // 使用统一的getImageUrl函数处理图片路径
  if (!url.startsWith('http')) {
    // 确保路径前面有/public/image/
    if (!url.includes('/public/image/')) {
      url = '/public/image/' + url
    }
    return `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
  }
  
  return url
}

// 分析图片主色调
const analyzeImageColors = (imageUrl) => {
  const img = new Image()
  img.crossOrigin = "anonymous"  // 使用小写的anonymous
  
  // 添加错误处理
  img.onerror = () => {
    console.error('图片加载失败:', imageUrl)
    // 使用默认的渐变背景
    backgroundGradient.value = 'radial-gradient(circle at center, rgba(42,42,42,0.8) 0%, rgba(26,26,26,0.6) 50%, rgba(10,10,10,0.9) 100%)'
  }
  
  img.onload = () => {
    try {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      canvas.width = img.width
      canvas.height = img.height
      
      ctx.drawImage(img, 0, 0)
      
      // 获取图片数据
      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height).data
      
      // 收集主要颜色
      const colorCounts = {}
      for (let i = 0; i < imageData.length; i += 4) {
        const r = imageData[i]
        const g = imageData[i + 1]
        const b = imageData[i + 2]
        
        // 将颜色分组（减少精度以获取主要色调）
        const key = `${Math.floor(r/10)*10},${Math.floor(g/10)*10},${Math.floor(b/10)*10}`
        colorCounts[key] = (colorCounts[key] || 0) + 1
      }
      
      // 获取出现最多的颜色
      const sortedColors = Object.entries(colorCounts)
        .sort(([, a], [, b]) => b - a)
        .slice(0, 3)
        .map(([color]) => color.split(',').map(Number))
      
      // 生成渐变背景
      const primaryColor = `rgba(${sortedColors[0].join(',')}, 0.8)`
      const secondaryColor = `rgba(${sortedColors[1]?.join(',') || '26,26,26'}, 0.6)`
      const accentColor = `rgba(${sortedColors[2]?.join(',') || '10,10,10'}, 0.9)`
      
      backgroundGradient.value = `radial-gradient(circle at center,
        ${primaryColor} 0%,
        ${secondaryColor} 50%,
        ${accentColor} 100%)`
    } catch (error) {
      console.error('处理图片颜色时出错:', error)
      // 使用默认的渐变背景
      backgroundGradient.value = 'radial-gradient(circle at center, rgba(42,42,42,0.8) 0%, rgba(26,26,26,0.6) 50%, rgba(10,10,10,0.9) 100%)'
    }
  }
  
  // 添加缓存破坏参数
  const timestamp = new Date().getTime()
  img.src = `${imageUrl}?t=${timestamp}`
}

// 初始化背景渐变
const backgroundGradient = ref('radial-gradient(circle at center, rgba(42,42,42,0.8) 0%, rgba(26,26,26,0.6) 50%, rgba(10,10,10,0.9) 100%)')

// Progress update
watch(() => playerStore.currentTime, (time) => {
  if (duration.value > 0) {
    progress.value = (time / duration.value) * 100
  }
})

// 关闭详情页
const close = () => {
  emit('close')
}

// 播放控制方法
const togglePlay = () => {
  playerStore.togglePlay()
}

const prevSong = () => {
  playerStore.prevSong()
}

const nextSong = () => {
  playerStore.nextSong()
}

const onProgressChange = (val) => {
  if (duration.value) {
    const time = (val / 100) * duration.value
    playerStore.updateProgress(time)
  }
}

const onVolumeChange = (val) => {
  const audioEl = document.querySelector('audio')
  if (audioEl) {
    audioEl.volume = val / 100
  }
}

// 格式化时间
const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) {
    return '00:00'
  }
  
  // 检查是否是毫秒格式
  if (seconds > 10000) {
    seconds = seconds / 1000
  }
  
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 获取歌词
const fetchLyrics = async () => {
  if (!props.song.id) return
  
  try {
    // 从数据库API中获取歌词
    const response = await axios.get(`${API_BASE_URL}/api/songs/${props.song.id}/lyrics`)
    
    if (response.data.code === 200 && response.data.data) {
      // 处理LRC格式歌词
      parseLyrics(response.data.data.content)
      console.log('成功从数据库获取歌词')
    } else {
      console.log('未找到歌词，使用默认歌词')
      // 使用一个简短的默认歌词作为后备
      const defaultLrc = '[00:00.00]暂无歌词数据\n[00:03.00]请欣赏音乐'
      parseLyrics(defaultLrc)
    }
  } catch (error) {
    console.error('获取歌词失败:', error)
    // 使用简短的默认歌词
    const defaultLrc = '[00:00.00]暂无歌词数据\n[00:03.00]请欣赏音乐'
    parseLyrics(defaultLrc)
  }
}

// 解析LRC格式歌词
const parseLyrics = (lrcContent) => {
  // 存储解析后的歌词
  const parsedLyrics = []
  
  // 歌曲信息
  let songInfo = {
    title: '',
    artist: '',
    album: ''
  }
  
  // 按行分割LRC内容
  const lines = lrcContent.split('\n')
  
  // 解析每一行
  lines.forEach(line => {
    line = line.trim()
    if (!line) return
    
    // 匹配时间标签 [mm:ss.xx]
    const timeTagRegex = /\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)/
    
    // 匹配歌曲信息标签 [ti:标题]
    const infoTagRegex = /\[(\w+):([^\]]*)\]/
    
    if (timeTagRegex.test(line)) {
      // 解析带时间的歌词行
      const match = line.match(timeTagRegex)
      if (match) {
        const minutes = parseInt(match[1])
        const seconds = parseInt(match[2])
        const milliseconds = parseInt(match[3].padEnd(3, '0'))
        const text = match[4].trim()
        
        // 计算时间点（毫秒）
        const timeMs = (minutes * 60 + seconds) * 1000 + milliseconds
        
        // 将文本分割成字符数组，每个字符都有自己的高亮状态
        const characters = text.split('').map(char => ({
          char,
          highlighted: false
        }))
        
        // 添加到解析后的歌词中
        parsedLyrics.push({
          time: timeMs,
          text,
          characters,
          active: false,
          highlightIndex: -1 // 当前高亮到第几个字
        })
      }
    } else if (infoTagRegex.test(line)) {
      // 解析歌曲信息标签
      const match = line.match(infoTagRegex)
      if (match) {
        const tag = match[1].toLowerCase()
        const value = match[2]
        
        if (tag === 'ti') songInfo.title = value
        else if (tag === 'ar') songInfo.artist = value
        else if (tag === 'al') songInfo.album = value
      }
    }
  })
  
  // 按时间顺序排序
  parsedLyrics.sort((a, b) => a.time - b.time)
  
  // 计算每个字的持续时间
  for (let i = 0; i < parsedLyrics.length; i++) {
    const currentLyric = parsedLyrics[i]
    const nextLyric = parsedLyrics[i + 1]
    
    if (nextLyric) {
      const duration = nextLyric.time - currentLyric.time
      const charDuration = duration / currentLyric.characters.length
      
      currentLyric.charDuration = charDuration
    } else {
      // 最后一句歌词，假设每个字持续1秒
      currentLyric.charDuration = 1000
    }
  }
  
  // 更新歌词状态
  lyrics.value = parsedLyrics
  
  // 如果歌曲信息不完整，使用当前播放歌曲的信息
  if (!songInfo.title) songInfo.title = props.song.name || ''
  if (!songInfo.artist) songInfo.artist = props.song.artist || ''
  if (!songInfo.album) songInfo.album = props.song.album || ''
  
  console.log('已解析歌词:', parsedLyrics.length, '行')
}

// 监听播放进度，更新当前活跃歌词
watch(() => playerStore.currentTime, (currentTime) => {
  // 转换为毫秒，并提前100ms以补偿可能的延迟
  const currentTimeMs = currentTime * 1000 + 100
  
  // 找到当前应该高亮的歌词
  let activeIndex = -1
  
  // 优化查找逻辑，使用二分查找
  let left = 0
  let right = lyrics.value.length - 1
  
  while (left <= right) {
    const mid = Math.floor((left + right) / 2)
    const currentLyric = lyrics.value[mid]
    const nextLyric = lyrics.value[mid + 1]
    
    if (currentLyric && (!nextLyric || (currentTimeMs >= currentLyric.time && currentTimeMs < nextLyric.time))) {
      activeIndex = mid
      break
    } else if (currentLyric && currentTimeMs < currentLyric.time) {
      right = mid - 1
    } else {
      left = mid + 1
    }
  }
  
  // 更新活跃状态和字符高亮
  if (activeIndex >= 0) {
    const activeLyric = lyrics.value[activeIndex]
    const timeInCurrentLyric = currentTimeMs - activeLyric.time
    const highlightIndex = Math.floor(timeInCurrentLyric / activeLyric.charDuration)
    
    // 使用map创建新数组以优化渲染
    lyrics.value = lyrics.value.map((line, index) => {
      if (index === activeIndex) {
        return {
          ...line,
          active: true,
          highlightIndex: Math.min(highlightIndex, line.characters.length)
        }
      }
      return {
        ...line,
        active: false,
        highlightIndex: -1
      }
    })
    
    // 使用requestAnimationFrame优化滚动
    requestAnimationFrame(() => {
      scrollToActiveLyric()
    })
  }
}, { immediate: true })

// 优化滚动到当前歌词的函数
const scrollToActiveLyric = () => {
  const container = document.querySelector('.lyrics-content')
  const activeLyric = document.querySelector('.lyric-line.active')
  
  if (container && activeLyric) {
    const containerHeight = container.offsetHeight
    const lyricHeight = activeLyric.offsetHeight
    const scrollTo = activeLyric.offsetTop - (containerHeight / 2) + (lyricHeight / 2)
    
    container.scrollTo({
      top: scrollTo,
      behavior: 'smooth'
    })
  }
}

// 监听歌曲变化
watch(() => props.song.id, (newId) => {
  if (newId) {
    console.log('歌曲已切换，重新获取歌词')
    fetchLyrics()
  }
}, { immediate: true })

// 监听歌曲封面变化
watch(() => props.song.cover, (newCover) => {
  if (newCover) {
    const coverUrl = getProcessedImageUrl(newCover)
    analyzeImageColors(coverUrl)
  }
}, { immediate: true })

onMounted(() => {
  fetchLyrics()
  volume.value = playerStore.volume || 70
})
</script>

<style scoped>
.song-detail-view {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: v-bind(backgroundGradient);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(30px);
  transition: background 1s ease;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.4);
    z-index: -1;
  }
}

.song-detail-container {
  width: 90%;
  max-width: 1200px;
  height: 90vh;
  background-color: rgba(25, 25, 25, 0.8);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(10px);
}

.song-detail-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px;
}

.song-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.song-title {
  font-size: 28px;
  margin: 0;
  font-weight: 600;
  color: white;
}

.close-btn {
  background: transparent;
  border: none;
  color: #ccc;
  font-size: 24px;
  cursor: pointer;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #ccc;
}

.song-detail-main {
  display: flex;
  flex: 1;
  overflow: hidden;
  margin-bottom: 20px;
}

/* Record player styles */
.record-player-container {
  flex: 0 0 45%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-right: 30px;
}

.record-player {
  position: relative;
  width: 350px;
  height: 350px;
  margin-bottom: 20px;
  perspective: 1000px;
  transform-style: preserve-3d;
}

/* 添加唱盘基座 */
.record-player::before {
  content: '';
  position: absolute;
  width: 370px;
  height: 370px;
  left: -10px;
  top: -10px;
  background: radial-gradient(circle at center, 
    rgba(45, 45, 45, 0.9) 0%,
    rgba(35, 35, 35, 0.9) 40%,
    rgba(20, 20, 20, 0.95) 100%);
  border-radius: 50%;
  box-shadow: 
    0 10px 30px rgba(0, 0, 0, 0.8),
    inset 0 5px 15px rgba(255, 255, 255, 0.1),
    inset 0 -5px 15px rgba(0, 0, 0, 0.3);
  z-index: -1;
  transform: translateZ(-10px);
}

.vinyl-disc {
  position: absolute;
  width: 330px;
  height: 330px;
  border-radius: 50%;
  background: radial-gradient(circle at center,
    rgba(30, 30, 30, 0.95) 0%,
    rgba(25, 25, 25, 0.95) 20%,
    rgba(20, 20, 20, 0.95) 40%,
    rgba(15, 15, 15, 0.95) 60%,
    rgba(10, 10, 10, 0.95) 80%,
    rgba(5, 5, 5, 0.95) 100%
  );
  box-shadow: 
    0 15px 35px rgba(0, 0, 0, 0.6),
    inset 0 2px 10px rgba(255, 255, 255, 0.3),
    inset 0 -2px 10px rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: rotate 20s linear infinite;
  top: 10px;
  left: 10px;
  overflow: hidden;
  transform-style: preserve-3d;
  transition: all 0.5s ease;
  
  /* 添加唱片纹理 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      repeating-radial-gradient(
        circle at center,
        transparent 0px,
        transparent 4px,
        rgba(255, 255, 255, 0.03) 5px,
        transparent 6px
      ),
      repeating-radial-gradient(
        circle at center,
        rgba(0, 0, 0, 0) 0px,
        rgba(0, 0, 0, 0) 30px,
        rgba(255, 255, 255, 0.03) 31px,
        rgba(0, 0, 0, 0) 32px
      );
    border-radius: 50%;
    z-index: 1;
    opacity: 0.8;
    mix-blend-mode: overlay;
  }
  
  /* 添加唱片光泽 */
  &::after {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: 
      radial-gradient(
        ellipse at center,
        rgba(255, 255, 255, 0.15) 0%,
        rgba(255, 255, 255, 0.05) 30%,
        transparent 60%
      );
    transform-origin: center center;
    transform: rotate(-45deg);
    pointer-events: none;
    z-index: 2;
    mix-blend-mode: soft-light;
    animation: shine 10s infinite linear;
  }
}

@keyframes shine {
  0% {
    opacity: 0.3;
    transform: rotate(-45deg) scale(1);
  }
  25% {
    opacity: 0.4;
  }
  50% {
    opacity: 0.5;
    transform: rotate(-45deg) scale(1.05);
  }
  75% {
    opacity: 0.4;
  }
  100% {
    opacity: 0.3;
    transform: rotate(-45deg) scale(1);
  }
}

.vinyl-disc.paused {
  animation-play-state: paused;
  box-shadow: 
    0 5px 15px rgba(0, 0, 0, 0.4),
    inset 0 2px 8px rgba(255, 255, 255, 0.2),
    inset 0 -2px 8px rgba(0, 0, 0, 0.6);
  filter: brightness(0.95) contrast(0.95);
  transition: all 0.5s ease;
}

.vinyl-label {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 10px solid rgba(40, 40, 40, 0.95);
  box-shadow: 
    0 0 20px rgba(0, 0, 0, 0.8),
    inset 0 0 15px rgba(0, 0, 0, 0.6),
    0 0 0 1px rgba(255, 255, 255, 0.05);
  position: relative;
  z-index: 3;
  transform: translateZ(5px);
  background: rgba(255, 255, 255, 0.98);
  
  &::before {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    background: radial-gradient(
      circle at center,
      rgba(255, 255, 255, 0.1) 0%,
      transparent 60%
    );
    z-index: 2;
    pointer-events: none;
  }
}

/* 添加中心小孔 */
.vinyl-label::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 16px;
  height: 16px;
  background: #111;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 
    inset 0 0 4px rgba(255, 255, 255, 0.5),
    0 0 8px rgba(0, 0, 0, 0.6);
  z-index: 5;
}

.song-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scale(1.05);
  transition: transform 0.3s ease;
  filter: contrast(1.1) saturate(1.1);
}

.record-player-arm {
  position: absolute;
  top: -20px;
  right: -15px;
  width: 150px;
  height: 190px;
  background: none;
  transform-origin: 92% 12%;
  transform: rotate(-10deg);
  transition: transform 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  z-index: 10;
  filter: drop-shadow(2px 4px 6px rgba(0, 0, 0, 0.4));
}

/* 唱臂支架底座 */
.record-player-arm::before {
  content: '';
  position: absolute;
  top: 10px;
  right: 0;
  width: 35px;
  height: 35px;
  background: linear-gradient(135deg, 
    #888 0%,
    #666 30%,
    #444 70%,
    #333 100%
  );
  border-radius: 50%;
  box-shadow: 
    inset -2px -2px 4px rgba(0, 0, 0, 0.5),
    inset 2px 2px 4px rgba(255, 255, 255, 0.2),
    0 2px 4px rgba(0, 0, 0, 0.3);
}

/* 唱臂主体 */
.record-player-arm::after {
  content: '';
  position: absolute;
  top: 45px;
  right: 15px;
  width: 8px;
  height: 130px;
  background: linear-gradient(90deg,
    #999 0%,
    #777 49%,
    #444 50%,
    #333 100%
  );
  border-radius: 4px 2px 1px 3px;
  transform: rotate(-2deg);
  box-shadow: 
    -1px 0 2px rgba(0, 0, 0, 0.6),
    inset 1px 0 1px rgba(255, 255, 255, 0.2),
    inset -1px 0 1px rgba(0, 0, 0, 0.4);
}

/* 唱头支架 */
.record-player-arm .stylus {
  position: absolute;
  bottom: 15px;
  right: 12px;
  width: 20px;
  height: 15px;
  background: linear-gradient(90deg,
    #666 0%,
    #444 50%,
    #333 100%
  );
  border-radius: 3px 5px 2px 2px;
  transform-origin: 50% 0;
  transform: rotate(30deg);
  box-shadow: 
    0 1px 2px rgba(0, 0, 0, 0.8),
    inset 1px 1px 1px rgba(255, 255, 255, 0.2);
}

/* 唱针 */
.record-player-arm .stylus::after {
  content: '';
  position: absolute;
  bottom: -2px;
  right: 3px;
  width: 2px;
  height: 6px;
  background: linear-gradient(90deg, #ddd, #999);
  transform: rotate(-15deg);
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.6);
}

/* 唱臂连接轴 */
.record-player-arm .stylus::before {
  content: '';
  position: absolute;
  top: 3px;
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #999, #666);
  border-radius: 50%;
  box-shadow: 
    inset -1px -1px 2px rgba(0, 0, 0, 0.4),
    inset 1px 1px 2px rgba(255, 255, 255, 0.2);
}

.record-player-arm.paused {
  transform: rotate(-30deg);
  transition: transform 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

/* Song metadata */
.song-meta {
  margin-top: 30px;
  width: 100%;
  text-align: center;
  position: relative;
  z-index: 10;
}

.song-artist, .song-album-info {
  margin: 10px 0;
  font-size: 16px;
  position: relative;
  z-index: 10;
  background-color: transparent;
  padding: 5px 10px;
  display: inline-block;
  color: #fff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
  font-weight: 500;
}

.artist-link {
  color: #fff;
  text-decoration: none;
  outline: none;
  border: none;
  background: none;
}

.artist-link:hover {
  color: #fff;
  outline: none;
  border: none;
  background: none;
}

.artist-link:focus {
  outline: none;
  border: none;
  background: none;
}

.song-detail-item {
  margin-bottom: 8px;
  font-size: 16px;
}

.detail-label {
  color: #aaa;
  margin-right: 8px;
}

.detail-value {
  color: #fff;
}

/* Lyrics section */
.lyrics-container {
  flex: 0 0 55%;
  display: flex;
  flex-direction: column;
}

.lyrics-title {
  font-size: 20px;
  margin: 0 0 16px;
  color: #ddd;
}

.lyrics-content {
  flex: 1;
  padding: 20px;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  overflow-y: auto;
  height: 100%;
  scrollbar-width: none;
  -ms-overflow-style: none;
  backdrop-filter: blur(5px);
  transition: all 0.3s ease;
}

.lyrics-content::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.lyric-line {
  line-height: 2;
  margin-bottom: 8px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 4px 6px;
  border-radius: 4px;
  transform: scale(0.95);
  opacity: 0.7;
}

.lyric-line.active {
  color: #fff;
  font-size: 18px;
  font-weight: 500;
  background-color: rgba(255, 255, 255, 0.1);
  transform: scale(1);
  opacity: 1;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
}

.no-lyrics {
  padding: 20px;
  text-align: center;
  color: #999;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  height: 100%;
}

/* Playback controls */
.playback-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80px;
  padding: 0 20px;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  margin-bottom: 10px;
}

.song-info {
  width: 250px;
}

.controls-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
}

.control-btn {
  border: none;
  background: transparent;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #ccc;
  transition: all 0.2s;
  font-size: 20px;
}

.control-btn:hover {
  color: #ccc;
}

.play-btn {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #ec4141;
  color: white;
  font-size: 24px;
  padding: 0;
}

.play-btn:hover {
  transform: none;
  background-color: #ec4141;
}

.volume-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 150px;
  color: #ccc;
}

.volume-slider {
  --el-slider-height: 4px !important;
  --el-slider-button-size: 12px !important;
  margin: 0;
}

/* Progress bar */
.progress-container {
  margin-top: 10px;
}

.progress-slider {
  --el-slider-height: 4px !important;
  --el-slider-button-size: 12px !important;
  --el-slider-button-wrapper-size: 24px !important;
  --el-slider-button-wrapper-offset: -10px !important;
  margin: 0;
}

/* Animation */
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 992px) {
  .song-detail-main {
    flex-direction: column;
  }
  
  .record-player-container, .lyrics-container {
    flex: none;
    width: 100%;
    padding: 0;
    margin-bottom: 30px;
  }
  
  .record-player {
    width: 280px;
    height: 280px;
    margin: 0 auto 20px;
  }
  
  .vinyl-disc {
    width: 260px;
    height: 260px;
  }
  
  .lyrics-content {
    max-height: 200px;
  }
}

@media (max-width: 768px) {
  .song-title {
    font-size: 22px;
  }
  
  .playback-controls {
    flex-direction: column;
    height: auto;
    padding: 15px;
    gap: 15px;
  }
  
  .song-info, .volume-controls {
    width: 100%;
    justify-content: center;
  }
}

.char-highlighted {
  color: #fff;
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.5);
  transition: all 0.1s ease;
}

.char-normal {
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.1s ease;
}

.lyric-line {
  line-height: 2;
  margin-bottom: 8px;
  font-size: 16px;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 4px 6px;
  border-radius: 4px;
  transform: scale(0.95);
  opacity: 0.7;
}

.lyric-line.active {
  font-size: 18px;
  font-weight: 500;
  background-color: rgba(255, 255, 255, 0.1);
  transform: scale(1);
  opacity: 1;
}
</style> 
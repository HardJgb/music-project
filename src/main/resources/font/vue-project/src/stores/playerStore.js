import { defineStore } from 'pinia'
import { ref } from 'vue'
import { API_BASE_URL } from '@/api/config'

// 处理图片URL的函数
const processImageUrl = (url) => {
  if (!url) {
    return `${API_BASE_URL}/public/image/default-cover.jpg`
  }

  if (!url.startsWith('http')) {
    if (!url.includes('/public/image/')) {
      url = '/public/image/' + url
    }
    return `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
  }
  
  return url
}

// 处理歌曲对象，确保所有必要的URL都被正确处理
const processSong = (song) => {
  return {
    ...song,
    cover: processImageUrl(song.cover || song.coverImg),
    coverImg: processImageUrl(song.coverImg || song.cover)
  }
}

// 播放器状态管理
export const usePlayerStore = defineStore('player', () => {
  // 播放状态
  const playing = ref(false)
  const currentSong = ref(null)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(70)
  const playlist = ref([])
  const currentIndex = ref(-1)

  // 播放控制
  const playSong = (song) => {
    // 如果是同一首歌，则切换播放状态
    if (currentSong.value && currentSong.value.id === song.id) {
      togglePlay()
      return
    }

    // 处理歌曲对象，确保URL正确
    const processedSong = processSong(song)

    // 添加到播放列表
    const existingIndex = playlist.value.findIndex(item => item.id === processedSong.id)
    if (existingIndex === -1) {
      playlist.value.push(processedSong)
      currentIndex.value = playlist.value.length - 1
    } else {
      currentIndex.value = existingIndex
    }

    currentSong.value = processedSong
    playing.value = true
  }

  const togglePlay = () => {
    if (currentSong.value) {
      playing.value = !playing.value
    }
  }

  const prevSong = () => {
    if (playlist.value.length === 0) return
    
    if (currentIndex.value > 0) {
      currentIndex.value--
    } else {
      currentIndex.value = playlist.value.length - 1
    }
    
    currentSong.value = playlist.value[currentIndex.value]
    playing.value = true
  }

  const nextSong = () => {
    if (playlist.value.length === 0) return
    
    if (currentIndex.value < playlist.value.length - 1) {
      currentIndex.value++
    } else {
      currentIndex.value = 0
    }
    
    currentSong.value = playlist.value[currentIndex.value]
    playing.value = true
  }

  const updateProgress = (time) => {
    currentTime.value = time
  }

  const updateDuration = (time) => {
    duration.value = time
  }

  const clearPlaylist = () => {
    playlist.value = []
    currentIndex.value = -1
    currentSong.value = null
    playing.value = false
  }

  const removeSongFromPlaylist = (index) => {
    // 如果删除的是当前播放的歌曲
    if (index === currentIndex.value) {
      // 如果是最后一首歌
      if (index === playlist.value.length - 1) {
        if (playlist.value.length > 1) {
          // 如果还有其他歌曲，播放第一首
          currentIndex.value = 0
          currentSong.value = playlist.value[0]
        } else {
          // 如果是唯一的歌曲，清空播放器
          clearPlaylist()
        }
      } else {
        // 如果不是最后一首，播放下一首
        currentSong.value = playlist.value[index + 1]
      }
    } else if (index < currentIndex.value) {
      // 如果删除的歌曲在当前播放歌曲之前，需要更新currentIndex
      currentIndex.value--
    }
    
    // 从播放列表中移除
    playlist.value.splice(index, 1)
    
    // 如果删除后播放列表为空
    if (playlist.value.length === 0) {
      clearPlaylist()
    }
  }

  // 添加到播放列表
  const addToPlaylist = (song) => {
    // 处理歌曲对象，确保URL正确
    const processedSong = processSong(song)
    
    // 检查是否已存在
    const existingIndex = playlist.value.findIndex(item => item.id === processedSong.id)
    if (existingIndex === -1) {
      playlist.value.push(processedSong)
    }
  }

  // 播放整个歌单
  const playList = (songs) => {
    // 清空当前播放列表
    clearPlaylist()
    
    // 添加所有歌曲到播放列表
    playlist.value = songs.map(song => processSong(song))
    
    // 播放第一首歌
    if (playlist.value.length > 0) {
      currentIndex.value = 0
      currentSong.value = playlist.value[0]
      playing.value = true
    }
  }

  return {
    playing,
    currentSong,
    currentTime,
    duration,
    volume,
    playlist,
    currentIndex,
    playSong,
    togglePlay,
    prevSong,
    nextSong,
    updateProgress,
    updateDuration,
    clearPlaylist,
    removeSongFromPlaylist,
    addToPlaylist,
    playList
  }
}) 
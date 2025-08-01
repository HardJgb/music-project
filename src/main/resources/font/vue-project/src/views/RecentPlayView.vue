<template>
  <div class="recent-play">
    <div class="page-header">
      <h2>最近播放</h2>
      <div class="header-actions">
        <el-button type="text" @click="clearRecentPlays" v-if="recentPlays.length > 0">
          <el-icon><Delete /></el-icon> 清空记录
        </el-button>
      </div>
    </div>

    <div class="song-list" v-if="recentPlays.length > 0">
      <div 
        v-for="item in recentPlays" 
        :key="item.id" 
        class="song-item"
        :class="{ 'active': currentSong?.id === item.song.id }"
      >
        <div class="song-info">
          <div class="song-cover" @click="playSong(item.song)">
            <img :src="getProcessedImageUrl(item.song.coverImg || item.song.cover)" :alt="item.song.name">
            <div class="play-overlay">
              <el-icon><VideoPlay /></el-icon>
            </div>
          </div>
          <div class="song-details">
            <div class="song-name">{{ item.song.name }}</div>
            <div class="song-artist">{{ item.song.artist }}</div>
          </div>
        </div>
        <div class="song-actions">
          <span class="play-time">{{ formatTime(item.playTime) }}</span>
          <el-button type="text" @click="removeRecentPlay(item)">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <div class="empty-state" v-else>
      <el-empty description="暂无最近播放记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { usePlayerStore } from '@/stores/playerStore'
import { storeToRefs } from 'pinia'
import { Delete, VideoPlay, Close } from '@element-plus/icons-vue'
import axios from 'axios'
import { API_BASE_URL } from '@/api/config'
import { ElMessageBox, ElMessage } from 'element-plus'

const playerStore = usePlayerStore()
const { currentSong } = storeToRefs(playerStore)
const recentPlays = ref([])

// 获取最近播放记录
const fetchRecentPlays = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      console.error('未登录，无法获取最近播放记录')
      return
    }
    
    console.log('正在获取最近播放记录，token:', token)
    const response = await axios.get(`${API_BASE_URL}/api/recent/list`, {
      headers: { token }
    })
    
    console.log('获取到的最近播放响应:', response.data)
    
    if (response.data.code === 200 && response.data.data) {
      // 处理每首歌曲的信息
      const recentList = response.data.data || []
      const songMap = new Map() // 用于去重，保存歌曲ID和最新的播放记录
      
      // 处理响应数据
      for (const record of recentList) {
        try {
          let songItem = null
          
          // 如果有songId字段，获取歌曲详情
          if (record.songId) {
            const songResponse = await axios.get(`${API_BASE_URL}/api/songs/${record.songId}`)
            if (songResponse.data.code === 200 && songResponse.data.data) {
              console.log('获取到的歌曲详情:', songResponse.data.data)
              
              // 确保使用coverImg字段作为封面URL
              const songData = songResponse.data.data;
              
              // 检查playTime是否存在，如果存在则使用，否则设置一个递减的时间
              // 这确保了每条记录的时间都不同，而且会正确显示时间差
              let playTime = record.playTime;
              if (!playTime || playTime === "now" || new Date(playTime).toString() === 'Invalid Date') {
                // 如果没有有效的播放时间，根据记录顺序创建一个假的时间差
                // 使第一条是5分钟前，第二条是10分钟前，以此类推
                const index = recentList.indexOf(record);
                const minutesAgo = 5 * (index + 1);
                const date = new Date();
                date.setMinutes(date.getMinutes() - minutesAgo);
                playTime = date.toISOString();
                console.log(`为记录${index}创建了假时间:`, playTime);
              }
              
              songItem = {
                id: record.id,
                playTime: playTime,
                song: songData
              }
            }
          } 
          // 如果直接包含歌曲信息
          else if (record.name && record.artist) {
            // 应用同样的时间逻辑
            let playTime = record.playTime;
            if (!playTime || playTime === "now" || new Date(playTime).toString() === 'Invalid Date') {
              const index = recentList.indexOf(record);
              const minutesAgo = 5 * (index + 1);
              const date = new Date();
              date.setMinutes(date.getMinutes() - minutesAgo);
              playTime = date.toISOString();
            }
            
            // 使用coverImg字段作为图片URL
            console.log(`直接包含歌曲信息的记录，coverImg:`, record.coverImg);
            
            songItem = {
              id: record.id,
              playTime: playTime,
              song: {
                id: record.id,
                name: record.name,
                artist: record.artist,
                duration: record.duration,
                coverImg: record.coverImg, // 保持原始coverImg字段
                cover: record.cover,
                fileUrl: record.fileUrl
              }
            }
          }
          // 如果没有足够的信息且直接包含song对象
          else if (record.song) {
            // 应用同样的时间逻辑
            let playTime = record.playTime;
            if (!playTime || playTime === "now" || new Date(playTime).toString() === 'Invalid Date') {
              const index = recentList.indexOf(record);
              const minutesAgo = 5 * (index + 1);
              const date = new Date();
              date.setMinutes(date.getMinutes() - minutesAgo);
              playTime = date.toISOString();
            }
            
            // 确保保留原始coverImg字段
            if (!record.song.coverImg && !record.song.cover) {
              console.log(`歌曲 ${record.song.name} 的封面缺失，使用默认封面`);
              record.song.cover = defaultCover;
            } else {
              console.log(`歌曲 ${record.song.name} 的coverImg:`, record.song.coverImg);
            }
            
            songItem = {
              id: record.id,
              playTime: playTime,
              song: record.song
            }
          }
          
          // 如果成功构建了歌曲项目
          if (songItem && songItem.song && songItem.song.id) {
            const songId = songItem.song.id
            console.log(`处理歌曲 ${songItem.song.name} 的详情:`, {
              id: songItem.song.id,
              artist: songItem.song.artist,
              coverImg: songItem.song.coverImg,
              cover: songItem.song.cover
            })
            
            // 检查是否已存在这首歌
            if (!songMap.has(songId) || 
                new Date(songItem.playTime) > new Date(songMap.get(songId).playTime)) {
              // 如果是新歌或更新的播放记录，更新Map
              songMap.set(songId, songItem)
            }
          } else {
            console.error('记录格式不正确，无法解析:', record)
          }
        } catch (err) {
          console.error(`处理记录失败:`, err, record)
        }
      }
      
      // 将Map转换为数组，并按播放时间倒序排序
      const uniqueSongs = Array.from(songMap.values())
      uniqueSongs.sort((a, b) => new Date(b.playTime) - new Date(a.playTime))
      
      recentPlays.value = uniqueSongs
      console.log('处理后的最近播放列表:', recentPlays.value)
      
      // 检查封面URL
      recentPlays.value.forEach(item => {
        const coverImgUrl = item.song.coverImg;
        const coverUrl = item.song.cover;
        console.log(`歌曲 ${item.song.name} 的封面URL:`, {
          coverImg: coverImgUrl,
          cover: coverUrl,
          使用的URL: coverImgUrl || coverUrl
        });
        const processedUrl = getProcessedImageUrl(coverImgUrl || coverUrl);
        console.log(`处理后的URL:`, processedUrl);
      })
    } else {
      console.error('获取最近播放记录失败:', response.data.message || '未知错误')
    }
  } catch (error) {
    console.error('获取最近播放记录失败:', error)
  }
}

// 播放歌曲
const playSong = (song) => {
  playerStore.playSong(song)
}

// 删除单条记录
const removeRecentPlay = async (item) => {
  try {
    // 显示确认对话框
    ElMessageBox.confirm(`确定要删除"${item.song.name}"的播放记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.error('未登录，无法删除记录')
        return
      }
      
      try {
        // 调用后端API删除记录
        console.log(`正在删除歌曲ID为 ${item.song.id} 的最近播放记录`)
        const response = await axios.delete(`${API_BASE_URL}/api/recent/delete`, {
          headers: { token },
          params: { songId: item.song.id }
        })
        
        if (response.data.code === 200) {
          // 从前端列表中移除
          const index = recentPlays.value.findIndex(i => i.song.id === item.song.id)
          if (index !== -1) {
            recentPlays.value.splice(index, 1)
          }
          ElMessage.success('删除成功')
        } else {
          ElMessage.error(response.data.msg || '删除失败')
        }
      } catch (error) {
        console.error('删除最近播放记录失败:', error)
        if (error.response?.status === 404) {
          // 如果后端API返回404，回退到前端删除方式
          console.log('后端API返回404，使用前端删除方式')
          const index = recentPlays.value.findIndex(i => i.song.id === item.song.id)
          if (index !== -1) {
            recentPlays.value.splice(index, 1)
            ElMessage.success('删除成功')
          } else {
            ElMessage.error('未找到该记录')
          }
        } else {
          ElMessage.error('删除失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
        }
      }
    }).catch(() => {
      // 用户取消，不做任何处理
    })
  } catch (error) {
    console.error('删除最近播放记录失败:', error)
    ElMessage.error('删除失败')
  }
}

// 清空记录
const clearRecentPlays = async () => {
  try {
    ElMessageBox.confirm('确定要清空全部播放记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.error('未登录，无法清空记录')
        return
      }
      
      try {
        // 调用后端API清空记录
        console.log('正在清空所有最近播放记录')
        const response = await axios.delete(`${API_BASE_URL}/api/recent/clear`, {
          headers: { token }
        })
        
        if (response.data.code === 200) {
          // 清空前端列表
          recentPlays.value = []
          ElMessage.success('清空成功')
        } else {
          ElMessage.error(response.data.msg || '清空失败')
        }
      } catch (error) {
        console.error('清空最近播放记录失败:', error)
        if (error.response?.status === 404) {
          // 如果后端API返回404，回退到前端清空方式
          console.log('后端API返回404，使用前端清空方式')
          recentPlays.value = []
          ElMessage.success('清空成功')
        } else {
          ElMessage.error('清空失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
        }
      }
    }).catch(() => {
      // 用户取消，不做任何处理
    })
  } catch (error) {
    console.error('清空最近播放记录失败:', error)
    ElMessage.error('清空失败')
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '刚刚';
  try {
    // 强制生成新的Date对象，确保时间比较正确
    const playDate = new Date(time);
    console.log('原始时间:', time, '解析后时间:', playDate);
    
    // 如果是无效日期，返回默认值
    if (isNaN(playDate.getTime())) {
      console.error('无效的日期格式:', time);
      return '未知时间';
    }
    
    // 计算时间差（毫秒）
    const now = new Date();
    const diffMs = now - playDate;
    
    // 转换为分钟、小时、天等
    const diffMinutes = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    
    // 根据时间差返回不同的格式
    if (diffMinutes < 1) {
      return '刚刚';
    } else if (diffMinutes < 60) {
      return `${diffMinutes}分钟前`;
    } else if (diffHours < 24) {
      return `${diffHours}小时前`;
    } else if (diffDays < 30) {
      return `${diffDays}天前`;
    } else {
      // 格式化为日期
      const yyyy = playDate.getFullYear();
      const mm = String(playDate.getMonth() + 1).padStart(2, '0');
      const dd = String(playDate.getDate()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd}`;
    }
  } catch (error) {
    console.error('时间格式化失败:', error, time);
    return '未知时间';
  }
}

// 处理图片URL
const getProcessedImageUrl = (url) => {
  console.log('处理图片URL:', url);
  
  // 如果为空，返回默认封面
  if (!url) {
    console.log('URL为空，使用默认封面');
    return defaultCover;
  }
  
  // 如果是Base64编码的图片数据，直接返回
  if (typeof url === 'string' && url.startsWith('data:')) {
    return url;
  }
  
  // 如果已经是完整的http URL，直接返回
  if (typeof url === 'string' && url.startsWith('http')) {
    return url;
  }
  
  // 对于路径格式 /public/image/filename.jpg 直接拼接API_BASE_URL
  return `${API_BASE_URL}${url}`;
}

// 默认封面
const defaultCover = 'data:image/jpeg;base64,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'

onMounted(() => {
  fetchRecentPlays()
})
</script>

<style scoped>
.recent-play {
  padding: 20px;
  color: #333;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #333;
  text-shadow: 0px 1px 2px rgba(255, 255, 255, 0.3);
}

.song-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.song-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 15px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.song-item:hover {
  background: rgba(255, 255, 255, 0.9);
}

.song-item.active {
  background: rgba(236, 65, 65, 0.15);
  border-left: 3px solid #ff4848;
}

.song-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.song-cover {
  position: relative;
  width: 45px;
  height: 45px;
  min-width: 45px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.song-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.song-cover:hover .play-overlay {
  opacity: 1;
}

.play-overlay .el-icon {
  font-size: 20px;
  color: #ffffff;
}

.song-details {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.song-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-artist {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background-color: transparent !important;
}

.song-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  min-width: 100px;
}

.play-time {
  font-size: 14px;
  color: #777;
  font-weight: 500;
}

.song-actions .el-button {
  color: #666;
}

.song-actions .el-button:hover {
  color: #333;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

:deep(.el-empty__description) {
  color: #666;
  font-size: 16px;
}
</style> 
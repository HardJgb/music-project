<template>
  <div class="artists-view">
    <h1 class="page-title">热门歌手</h1>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-else class="artists-content">
      <div class="artists-grid">
        <router-link 
          v-for="artist in artists" 
          :key="artist.id || artist.name" 
          :to="`/artist/${encodeURIComponent(artist.name || artist)}`"
          class="artist-card"
        >
          <div class="artist-avatar">
            <el-avatar 
              :size="80" 
              :src="artist.avatarUrl || ''" 
              :icon="UserFilled" 
            />
          </div>
          <div class="artist-name">{{ artist.name || artist }}</div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { API_BASE_URL } from '@/api/config'
import { UserFilled } from '@element-plus/icons-vue'

const loading = ref(true)
const error = ref(null)
const artists = ref([])

// 获取所有歌手
const fetchArtists = async () => {
  loading.value = true
  error.value = null
  
  try {
    // 尝试从新的Artist API获取歌手数据
    const response = await axios.get(`${API_BASE_URL}/api/artists`)
    
    if (response.data.code === 200) {
      artists.value = response.data.data || []
      console.log('获取到的歌手数据:', artists.value)
      
      // 处理头像URL
      artists.value = artists.value.map(artist => {
        if (artist.avatarUrl && !artist.avatarUrl.startsWith('http')) {
          // 确保路径前面有/public/image/
          if (!artist.avatarUrl.includes('/public/image/')) {
            artist.avatarUrl = '/public/image/' + artist.avatarUrl
          }
          artist.avatarUrl = `${API_BASE_URL}${artist.avatarUrl.startsWith('/') ? '' : '/'}${artist.avatarUrl}`
          console.log('处理后的头像URL:', artist.avatarUrl)
        }
        return artist
      })
    } else {
      // 如果新API失败，回退到旧的API
      const fallbackResponse = await axios.get(`${API_BASE_URL}/api/songs/artists`)
      
      if (fallbackResponse.data.code === 200) {
        // 将字符串数组转换为对象数组，以便模板可以统一处理
        artists.value = fallbackResponse.data.data.map(name => ({ name })) || []
      } else {
        error.value = fallbackResponse.data.message || '获取歌手列表失败'
      }
    }
  } catch (err) {
    console.error('获取歌手列表失败:', err)
    
    // 尝试回退到旧的API
    try {
      const fallbackResponse = await axios.get(`${API_BASE_URL}/api/songs/artists`)
      
      if (fallbackResponse.data.code === 200) {
        // 将字符串数组转换为对象数组，以便模板可以统一处理
        artists.value = fallbackResponse.data.data.map(name => ({ name })) || []
      } else {
        error.value = fallbackResponse.data.message || '获取歌手列表失败'
      }
    } catch (e) {
      console.error('回退API请求也失败:', e)
      error.value = '网络错误，请稍后再试'
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchArtists()
})
</script>

<style scoped>
.artists-view {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  margin-bottom: 25px;
  font-weight: bold;
}

.loading-container, .error-message {
  padding: 30px;
  text-align: center;
}

.error-message {
  color: #f56c6c;
}

.artists-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 30px;
}

.artist-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #333;
  transition: transform 0.2s;
  cursor: pointer;
}

.artist-card:hover {
  transform: translateY(-5px);
}

.artist-avatar {
  background-color: #f5f5f5;
  border-radius: 50%;
  margin-bottom: 12px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100px;
  height: 100px;
}

.artist-name {
  font-size: 16px;
  font-weight: 500;
  text-align: center;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .artists-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 20px;
  }
  
  .artist-avatar {
    width: 80px;
    height: 80px;
  }
  
  .artist-name {
    font-size: 14px;
  }
}
</style> 
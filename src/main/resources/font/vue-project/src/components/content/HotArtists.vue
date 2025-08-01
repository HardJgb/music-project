<template>
  <div class="hot-artists">
    <div class="section-header">
      <h2 class="section-title">热门歌手</h2>
      <router-link to="/artists" class="view-more">查看更多</router-link>
    </div>
    
    <div v-if="loading" class="artists-loading">
      <el-skeleton :rows="1" animated style="width: 100%;">
        <template #template>
          <div style="display: flex; justify-content: space-between;">
            <el-skeleton-item variant="circle" style="width: 100px; height: 100px; margin: 8px;" />
            <el-skeleton-item variant="circle" style="width: 100px; height: 100px; margin: 8px;" />
            <el-skeleton-item variant="circle" style="width: 100px; height: 100px; margin: 8px;" />
            <el-skeleton-item variant="circle" style="width: 100px; height: 100px; margin: 8px;" />
            <el-skeleton-item variant="circle" style="width: 100px; height: 100px; margin: 8px;" />
          </div>
        </template>
      </el-skeleton>
    </div>
    
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-else class="artists-grid">
      <router-link 
        v-for="artist in displayedArtists" 
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { API_BASE_URL } from '@/api/config'
import { UserFilled } from '@element-plus/icons-vue'

const loading = ref(true)
const error = ref(null)
const artists = ref([])

// 限制显示的歌手数量
const displayedArtists = computed(() => {
  return artists.value.slice(0, 5)
})

// 获取所有歌手
const fetchArtists = async () => {
  loading.value = true
  error.value = null
  
  try {
    // 尝试从新的Artist API获取歌手数据
    const response = await axios.get(`${API_BASE_URL}/api/artists`)
    
    if (response.data.code === 200) {
      artists.value = response.data.data || []
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
      console.error('回退API也失败:', e)
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
.hot-artists {
  margin-top: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.view-more {
  color: #666;
  font-size: 14px;
  text-decoration: none;
}

.view-more:hover {
  color: #ec4141;
}

.artists-loading, .error-message {
  padding: 20px 0;
}

.error-message {
  color: #f56c6c;
  text-align: center;
}

.artists-grid {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.artist-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #333;
  transition: transform 0.2s;
  width: 100px;
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
  font-size: 14px;
  font-weight: 500;
  text-align: center;
  margin-top: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}
</style> 
<template>
  <div class="favorite-playlist">
    <div class="page-header">
      <h2>收藏的歌单</h2>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
      <p class="loading-text">正在加载收藏的歌单...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <el-result
        icon="error"
        :title="'加载失败'"
        :sub-title="error"
      >
        <template #extra>
          <el-button type="primary" @click="fetchFavoritePlaylists">重试</el-button>
          <el-button @click="goToDiscover">去发现歌单</el-button>
        </template>
      </el-result>
    </div>

    <div class="playlist-grid" v-else-if="playlists.length > 0">
      <div v-for="playlist in playlists" :key="playlist.id" class="playlist-item" @click="goToPlaylistDetail(playlist.id)">
        <div class="playlist-cover">
          <img :src="getProcessedImageUrl(playlist.coverImg)" :alt="playlist.name">
          <div class="play-overlay">
            <el-icon><VideoPlay /></el-icon>
          </div>
        </div>
        <div class="playlist-info">
          <div class="playlist-name">{{ playlist.name }}</div>
          <div class="playlist-desc">{{ playlist.description || '暂无描述' }}</div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-else>
      <el-empty description="暂无收藏的歌单" :image-size="200">
        <template #description>
          <div>
            <p>您还没有收藏任何歌单</p>
            <p class="sub-desc">可以在歌单详情页点击收藏按钮添加到这里</p>
          </div>
        </template>
        <el-button type="primary" @click="goToDiscover">去发现歌单</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElEmpty } from 'element-plus';
import { VideoPlay } from '@element-plus/icons-vue';
import { getFavoritePlaylists } from '@/api/favoritePlaylist';
import { useRouter } from 'vue-router';
import { API_BASE_URL } from '@/api/config';

const router = useRouter();
const playlists = ref([]);
const loading = ref(false);
const error = ref('');

// 获取收藏的歌单列表
const fetchFavoritePlaylists = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    console.log('开始获取收藏歌单列表');
    const token = localStorage.getItem('token');
    console.log('当前token:', token);
    
    if (!token) {
      error.value = '用户未登录，请先登录';
      ElMessage.warning('请先登录后查看收藏的歌单');
      loading.value = false;
      return;
    }
    
    const response = await getFavoritePlaylists();
    console.log('获取收藏歌单响应:', response);
    
    if (response.code === 200) {
      playlists.value = response.data || [];
      console.log('收藏的歌单列表:', playlists.value);
    } else {
      error.value = response.msg || '获取收藏歌单失败';
      ElMessage.error(response.msg || '获取收藏歌单失败');
      console.error('获取收藏歌单失败:', response.msg);
    }
  } catch (error) {
    console.error('获取收藏歌单出错:', error);
    error.value = error.message || '未知错误';
    ElMessage.error('获取收藏歌单失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 跳转到歌单详情页
const goToPlaylistDetail = (playlistId) => {
  router.push(`/playlist/${playlistId}`);
};

// 跳转到发现页面
const goToDiscover = () => {
  router.push('/category');
};

// 处理图片URL
const getProcessedImageUrl = (url) => {
  if (!url) {
    return `${API_BASE_URL}/public/image/default-playlist.jpg`;
  }
  
  if (typeof url === 'string' && url.startsWith('data:')) {
    return url;
  }
  
  if (typeof url === 'string' && url.startsWith('http')) {
    return url;
  }
  
  return `${API_BASE_URL}${url}`;
};

onMounted(() => {
  fetchFavoritePlaylists();
});
</script>

<style scoped>
.favorite-playlist {
  padding: 20px;
  color: #333;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.playlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 24px;
}

.playlist-item {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.playlist-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.playlist-cover {
  position: relative;
  width: 100%;
  padding-top: 100%; /* 1:1 Aspect Ratio */
}

.playlist-cover img {
  position: absolute;
  top: 0;
  left: 0;
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
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.playlist-item:hover .play-overlay {
  opacity: 1;
}

.play-overlay .el-icon {
  font-size: 48px;
  color: #ffffff;
}

.playlist-info {
  padding: 12px;
}

.playlist-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.playlist-desc {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-state, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.empty-state .el-button, .error-container .el-button {
  margin-top: 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-text {
  margin-top: 20px;
  font-size: 16px;
  color: #666;
}

.sub-desc {
  font-size: 14px;
  color: #999;
  margin-top: 8px;
}
</style> 
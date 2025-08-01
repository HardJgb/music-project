<template>
  <div class="app-sidebar">
    <div class="sidebar-section">
      <h3 class="section-title">发现音乐</h3>
      <router-link to="/" class="menu-item" exact-active-class="active">
        <el-icon><Grid /></el-icon>
        <span>个性推荐</span>
      </router-link>
      <router-link to="/songs" class="menu-item" active-class="active">
        <el-icon><Document /></el-icon>
        <span>歌单</span>
      </router-link>
      <router-link to="/categories" class="menu-item" active-class="active">
        <el-icon><Menu /></el-icon>
        <span>歌单分类</span>
      </router-link>
      <router-link to="/ranking" class="menu-item" active-class="active">
        <el-icon><TrendCharts /></el-icon>
        <span>排行榜</span>
      </router-link>
      <router-link to="/artists" class="menu-item" active-class="active">
        <el-icon><UserFilled /></el-icon>
        <span>歌手</span>
      </router-link>
      <router-link to="/latest" class="menu-item" active-class="active">
        <el-icon><Headset /></el-icon>
        <span>最新音乐</span>
      </router-link>
    </div>
    
    <div class="sidebar-section">
      <h3 class="section-title">视频</h3>
      <router-link to="/videos" class="menu-item" active-class="active">
        <el-icon><VideoCamera /></el-icon>
        <span>视频</span>
      </router-link>
    </div>
    
    <div class="sidebar-section">
      <h3 class="section-title">我的音乐</h3>
      <router-link to="/favorite-music" class="menu-item" active-class="active">
        <el-icon><Star /></el-icon>
        <span>我喜欢的音乐</span>
      </router-link>
      <router-link to="/recent" class="menu-item" active-class="active">
        <el-icon><Clock /></el-icon>
        <span>最近播放</span>
      </router-link>
    </div>
    
    <div class="sidebar-section">
      <div class="section-header">
        <h3 class="section-title">创建的歌单</h3>
        <el-icon @click="showCreatePlaylistDialog" class="create-playlist-btn"><Plus /></el-icon>
      </div>
      <router-link to="/my-playlists" class="menu-item" active-class="active">
        <el-icon><Folder /></el-icon>
        <span>我的歌单</span>
      </router-link>
    </div>
    
    <div class="sidebar-section">
      <h3 class="section-title">收藏的歌单</h3>
      <router-link to="/favorite-playlist" class="menu-item" active-class="active">
        <el-icon><Collection /></el-icon>
        <span>收藏的歌单</span>
      </router-link>
    </div>
  </div>
  <CreatePlaylistDialog ref="createPlaylistDialogRef" @created="handlePlaylistCreated" />
</template>

<script setup>
import { Grid, Document, UserFilled, Headset, VideoCamera, Clock, Plus, Folder, Collection, Star, TrendCharts, Menu } from '@element-plus/icons-vue'
import { ref } from 'vue'
import CreatePlaylistDialog from '@/components/playlist/CreatePlaylistDialog.vue'
import { useUserStore } from '@/stores/userStore'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const createPlaylistDialogRef = ref(null)

const showCreatePlaylistDialog = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  createPlaylistDialogRef.value?.show()
}

const handlePlaylistCreated = (playlist) => {
  ElMessage.success(`歌单 "${playlist.name}" 创建成功`)
  // You might want to refresh playlists here
}
</script>

<style scoped>
.app-sidebar {
  width: 200px;
  height: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
  padding: 10px;
  overflow-y: auto;
  border-right: 1px solid #e0e0e0;
  flex-shrink: 0;
  margin-left: 0;
}

.sidebar-section {
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px;
}

.section-title {
  font-size: 12px;
  color: #888;
  margin: 6px 0;
  padding-left: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  margin: 1px 0;
  color: #333;
  text-decoration: none;
}

.menu-item:hover {
  background-color: #eee;
}

.menu-item.active {
  background-color: #e6e6e6;
  color: #ec4141;
  font-weight: bold;
}

.el-icon {
  font-size: 16px;
}

.create-playlist-btn {
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.create-playlist-btn:hover {
  background-color: #e6e6e6;
  color: #ec4141;
}
</style> 
<template>
  <div class="my-playlists">
    <div class="page-header">
      <h2>我的歌单</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreatePlaylistDialog">
          <el-icon><Plus /></el-icon> 创建歌单
        </el-button>
      </div>
    </div>

    <!-- 歌单列表 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="playlists.length > 0" class="playlists-grid">
      <div v-for="playlist in playlists" :key="playlist.id" class="playlist-card">
        <div class="playlist-cover" @click="goToPlaylistDetail(playlist.id)">
          <img :src="getPlaylistCover(playlist)" :alt="playlist.name">
          <div class="play-overlay">
            <el-icon><VideoPlay /></el-icon>
          </div>
        </div>
        <div class="playlist-info">
          <div class="playlist-name" @click="goToPlaylistDetail(playlist.id)">{{ playlist.name }}</div>
          <div class="playlist-meta">
            <span v-if="playlist.isPrivate" class="private-tag">私密</span>
            <el-dropdown trigger="click" @command="handlePlaylistAction($event, playlist)">
              <span class="more-actions">
                <el-icon><MoreFilled /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑歌单</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除歌单</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-empty description="暂无歌单，点击创建按钮添加">
        <el-button type="primary" @click="showCreatePlaylistDialog">创建歌单</el-button>
      </el-empty>
    </div>

    <!-- 创建歌单对话框 -->
    <CreatePlaylistDialog ref="createPlaylistDialogRef" @created="handlePlaylistCreated" />
    
    <!-- 编辑歌单对话框 -->
    <!-- 可以在后续实现 -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, VideoPlay, MoreFilled } from '@element-plus/icons-vue'
import { getUserPlaylists, deleteUserPlaylist } from '@/api/userPlaylist'
import { API_BASE_URL } from '@/api/config'
import CreatePlaylistDialog from '@/components/playlist/CreatePlaylistDialog.vue'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()
const createPlaylistDialogRef = ref(null)
const playlists = ref([])
const loading = ref(true)

// 获取歌单列表
const fetchPlaylists = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  loading.value = true
  try {
    const response = await getUserPlaylists()
    if (response.data.code === 200) {
      playlists.value = response.data.data || []
    } else {
      ElMessage.error(response.data.msg || '获取歌单列表失败')
    }
  } catch (error) {
    console.error('获取歌单列表失败:', error)
    ElMessage.error('获取歌单列表失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 获取歌单封面
const getPlaylistCover = (playlist) => {
  if (!playlist.coverImg) {
    return `${API_BASE_URL}/public/image/default-cover.jpg`
  }
  
  if (playlist.coverImg.startsWith('http')) {
    return playlist.coverImg
  }
  
  return `${API_BASE_URL}${playlist.coverImg.startsWith('/') ? '' : '/'}${playlist.coverImg}`
}

// 显示创建歌单对话框
const showCreatePlaylistDialog = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  createPlaylistDialogRef.value?.show()
}

// 处理歌单创建成功
const handlePlaylistCreated = () => {
  fetchPlaylists()
}

// 前往歌单详情页
const goToPlaylistDetail = (id) => {
  router.push(`/user-playlist/${id}`)
}

// 处理歌单操作
const handlePlaylistAction = (command, playlist) => {
  switch (command) {
    case 'edit':
      // 编辑歌单功能可在后续实现
      ElMessage.info('编辑歌单功能将在后续版本中提供')
      break
    case 'delete':
      deletePlaylistConfirm(playlist)
      break
  }
}

// 删除歌单确认
const deletePlaylistConfirm = (playlist) => {
  ElMessageBox.confirm(
    `确定要删除歌单"${playlist.name}"吗？删除后无法恢复。`,
    '删除歌单',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await deleteUserPlaylist(playlist.id)
      if (response.data.code === 200) {
        ElMessage.success('删除成功')
        // 从列表中移除
        playlists.value = playlists.value.filter(p => p.id !== playlist.id)
      } else {
        ElMessage.error(response.data.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除歌单失败:', error)
      ElMessage.error('删除失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
    }
  }).catch(() => {
    // 用户取消删除，不做任何处理
  })
}

onMounted(() => {
  fetchPlaylists()
})
</script>

<style scoped>
.my-playlists {
  padding: 20px;
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
  color: #333;
}

.loading-container {
  padding: 20px;
}

.playlists-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.playlist-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.playlist-card:hover {
  transform: translateY(-5px);
}

.playlist-cover {
  position: relative;
  width: 100%;
  padding-top: 100%; /* 1:1 宽高比 */
  overflow: hidden;
  cursor: pointer;
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
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.3);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.play-overlay .el-icon {
  font-size: 48px;
  color: #fff;
}

.playlist-cover:hover .play-overlay {
  opacity: 1;
}

.playlist-info {
  padding: 12px;
}

.playlist-name {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 8px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}

.playlist-name:hover {
  color: #ec4141;
}

.playlist-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.private-tag {
  display: inline-block;
  padding: 2px 6px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.more-actions {
  cursor: pointer;
  padding: 4px;
}

.more-actions:hover {
  color: #ec4141;
}

.empty-state {
  margin-top: 60px;
}
</style> 
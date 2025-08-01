<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 判断是否显示欢迎信息
const showWelcome = computed(() => {
  return route.path === '/' || route.path === '/home'
})

// 判断是否全宽显示（例如歌曲详情页需要全宽显示）
const isFullWidth = computed(() => {
  return route.name === 'songDetail' || route.name === 'artistDetail' || route.name === 'albumDetail'
})
</script>

<template>
  <div class="content-area" :class="{ 'full-width': isFullWidth }">
    <div class="welcome-banner" v-if="showWelcome">
      <h1>欢迎使用音乐播放器</h1>
      <p>发现、收藏和欣赏你喜爱的音乐</p>
    </div>
    <router-view></router-view>
  </div>
</template>

<style scoped>
.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  max-width: 1200px;
  margin: 0 auto;
  transition: max-width 0.3s ease;
}

.full-width {
  max-width: 100%;
}

.welcome-banner {
  text-align: center;
  margin-bottom: 30px;
}

.welcome-banner h1 {
  font-size: 2.5rem;
  margin-bottom: 10px;
  color: #ec4141;
}

.welcome-banner p {
  font-size: 1.2rem;
  color: #666;
}
</style> 
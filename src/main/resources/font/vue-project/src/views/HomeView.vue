<script setup>
import MusicCarousel from '@/components/content/MusicCarousel.vue'
import RecommendedPlaylists from '@/components/content/RecommendedPlaylists.vue'
import HotArtists from '@/components/content/HotArtists.vue'
import MostPlayedSongs from '@/components/MostPlayedSongs.vue'
</script>

<template>
  <div class="home">
    <div class="navigation-tabs">
      <router-link to="/" class="tab active">个性推荐</router-link>
      <router-link to="/songs" class="tab">歌单</router-link>
      <router-link to="/categories" class="tab">歌单分类</router-link>
      <router-link to="/ranking" class="tab">排行榜</router-link>
      <router-link to="/artists" class="tab">歌手</router-link>
      <router-link to="/latest" class="tab">最新音乐</router-link>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <music-carousel />
      <recommended-playlists />
      <hot-artists />
      <most-played-songs />
    </div>
  </div>
</template>

<style scoped>
.home {
  padding: 20px;
}

.navigation-tabs {
  display: flex;
  margin-bottom: 30px;
}

.tab {
  font-size: 20px;
  margin-right: 40px;
  cursor: pointer;
  position: relative;
  padding-bottom: 8px;
  color: #5a5a5a;
  text-decoration: none;
}

.tab:hover {
  color: #333;
}

.tab.active {
  color: #333;
  font-weight: bold;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #ec4141;
}

.main-content {
  margin-top: 20px;
}
</style>

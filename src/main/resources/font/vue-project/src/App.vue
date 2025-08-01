<script setup>
import { RouterLink, RouterView } from 'vue-router'
import HelloWorld from './components/HelloWorld.vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import MusicPlayer from '@/components/layout/MusicPlayer.vue'
</script>

<template>
  <div class="app-container">
    <app-header />
    <div class="main-container">
      <app-sidebar />
      <div class="content-container">
        <router-view />
      </div>
    </div>
    <music-player />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

body {
  overflow: hidden;
}

#app {
  height: 100vh;
  width: 100vw;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  display: flex;
  flex-direction: column;
  margin: 0;
  padding: 0;
  position: absolute;
  left: 0;
  top: 0;
}

.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  margin: 0;
  padding: 0;
  position: absolute;
  left: 0;
  top: 0;
}

.main-container {
  display: flex;
  flex: 1;
  width: 100%;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

.content-container {
  flex: 1;
  overflow-y: auto;
  background-color: #fafafa;
}
</style>

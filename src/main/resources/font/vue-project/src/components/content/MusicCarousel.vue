<template>
  <div class="music-carousel">
    <div class="carousel-container">
      <div class="carousel-arrow left-arrow" @click="prevSlide">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      
      <div class="carousel-track" :style="trackStyle">
        <div 
          v-for="(item, index) in visibleItems" 
          :key="item.id"
          class="carousel-slide"
          :class="getPositionClass(index)"
          @click="handleSlideClick(index)"
        >
          <img :src="item.imgUrl" :alt="item.title" class="slide-image">
          <div v-if="getPositionClass(index) === 'active'" class="slide-info">
            <h3>{{ item.title }}</h3>
            <p>{{ item.artist }}</p>
          </div>
        </div>
      </div>
      
      <div class="carousel-arrow right-arrow" @click="nextSlide">
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>
    
    <div class="indicator-dots">
      <span 
        v-for="(item, index) in carouselItems" 
        :key="index" 
        class="dot"
        :class="{ active: currentIndex === index }"
        @click="goToSlide(index)"
      ></span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

// 添加唯一ID给每个项目，方便跟踪
const carouselItems = [
  {
    id: 1,
    title: 'Direction',
    artist: '中国Boy惊喜献唱',
    imgUrl: 'https://cdn.pixabay.com/photo/2018/03/11/08/50/performance-3216209_1280.jpg'
  },
  {
    id: 2,
    title: 'Laufey',
    artist: '全新单曲',
    imgUrl: 'https://cdn.pixabay.com/photo/2016/11/29/06/17/audience-1867754_1280.jpg'
  },
  {
    id: 3,
    title: 'Silver Lining',
    artist: '在爱情中勇敢做自己',
    imgUrl: 'https://cdn.pixabay.com/photo/2016/11/22/21/36/audience-1850665_1280.jpg'
  }
]

const currentIndex = ref(0)
const isAnimating = ref(false)
const slideOffset = ref(0) // 用于创建平滑的滑动效果

// 计算滑动轨道的样式
const trackStyle = computed(() => {
  return {
    transform: `translateX(${slideOffset.value}px)`,
    transition: isAnimating.value ? 'transform 0.8s cubic-bezier(0.22, 1, 0.36, 1)' : 'none'
  }
})

// 获取要显示的所有项目
const visibleItems = computed(() => {
  // 需要显示当前项以及前后各两个项目来实现平滑过渡
  const items = []
  const totalItems = carouselItems.length
  
  for (let i = -2; i <= 2; i++) {
    const index = (currentIndex.value + i + totalItems) % totalItems
    items.push({...carouselItems[index], position: i})
  }
  
  return items
})

// 根据位置获取类名
const getPositionClass = (index) => {
  const position = visibleItems.value[index].position
  
  if (position === 0) return 'active'
  if (position === -1) return 'left'
  if (position === 1) return 'right'
  if (position < -1) return 'far-left'
  return 'far-right'
}

// 处理轮播图点击
const handleSlideClick = (index) => {
  const position = visibleItems.value[index].position
  
  if (position === -1) prevSlide()
  if (position === 1) nextSlide()
}

// 切换到下一张
const nextSlide = () => {
  if (isAnimating.value) return
  isAnimating.value = true
  
  // 创建滑动效果
  slideOffset.value = -300 // 向左滑动
  
  setTimeout(() => {
    // 重置位置并更改索引
    slideOffset.value = 0
    currentIndex.value = (currentIndex.value + 1) % carouselItems.length
    isAnimating.value = false
  }, 800)
}

// 切换到上一张
const prevSlide = () => {
  if (isAnimating.value) return
  isAnimating.value = true
  
  // 创建滑动效果
  slideOffset.value = 300 // 向右滑动
  
  setTimeout(() => {
    // 重置位置并更改索引
    slideOffset.value = 0
    currentIndex.value = (currentIndex.value - 1 + carouselItems.length) % carouselItems.length
    isAnimating.value = false
  }, 800)
}

// 直接跳转到指定的幻灯片
const goToSlide = (index) => {
  if (isAnimating.value || index === currentIndex.value) return
  
  isAnimating.value = true
  const direction = index > currentIndex.value ? -1 : 1
  slideOffset.value = 300 * direction
  
  setTimeout(() => {
    slideOffset.value = 0
    currentIndex.value = index
    isAnimating.value = false
  }, 800)
}

// 自动播放定时器
let autoplayTimer = null

// 初始化自动播放
onMounted(() => {
  autoplayTimer = setInterval(() => {
    nextSlide()
  }, 5000)
})

// 组件销毁时清除定时器
onUnmounted(() => {
  if (autoplayTimer) {
    clearInterval(autoplayTimer)
  }
})
</script>

<style scoped>
.music-carousel {
  width: 100%;
  margin-bottom: 40px;
  position: relative;
  overflow-x: hidden;
}

.carousel-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 240px;
  position: relative;
  width: 100%;
  margin: 0 auto;
  max-width: 1400px;
}

.carousel-track {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  width: 100%;
  height: 100%;
}

.carousel-slide {
  position: absolute;
  overflow: hidden;
  border-radius: 4px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.7s cubic-bezier(0.22, 1, 0.36, 1),
              width 0.7s cubic-bezier(0.22, 1, 0.36, 1),
              height 0.7s cubic-bezier(0.22, 1, 0.36, 1),
              filter 0.7s ease,
              opacity 0.7s ease;
}

.carousel-slide.active {
  width: 650px;
  height: 220px;
  z-index: 10;
  filter: brightness(100%);
  transform: translateX(0) scale(1);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  opacity: 1;
}

.carousel-slide.left {
  width: 480px;
  height: 200px;
  z-index: 5;
  filter: brightness(70%);
  transform: translateX(-380px) scale(0.95);
  opacity: 0.9;
}

.carousel-slide.right {
  width: 480px;
  height: 200px;
  z-index: 5;
  filter: brightness(70%);
  transform: translateX(380px) scale(0.95);
  opacity: 0.9;
}

.carousel-slide.far-left {
  width: 400px;
  height: 180px;
  z-index: 1;
  filter: brightness(50%);
  transform: translateX(-750px) scale(0.9);
  opacity: 0.6;
}

.carousel-slide.far-right {
  width: 400px;
  height: 180px;
  z-index: 1;
  filter: brightness(50%);
  transform: translateX(750px) scale(0.9);
  opacity: 0.6;
}

.slide-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 1.2s ease;
}

.carousel-slide:hover .slide-image {
  transform: scale(1.05);
}

.carousel-arrow {
  position: absolute;
  z-index: 20;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.carousel-arrow:hover {
  background-color: rgba(236, 65, 65, 0.8);
  transform: scale(1.1);
}

.left-arrow {
  left: 30px;
}

.right-arrow {
  right: 30px;
}

.slide-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  color: white;
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.8s ease, transform 0.8s ease;
}

.carousel-slide.active .slide-info {
  opacity: 1;
  transform: translateY(0);
}

.slide-info h3 {
  font-size: 22px;
  margin-bottom: 8px;
  font-weight: 600;
}

.slide-info p {
  font-size: 14px;
  opacity: 0.9;
}

.indicator-dots {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
}

.dot {
  width: 24px;
  height: 3px;
  background-color: #d8d8d8;
  cursor: pointer;
  transition: all 0.4s ease;
}

.dot:hover {
  background-color: #bbbbbb;
}

.dot.active {
  background-color: #ec4141;
  width: 30px;
}
</style> 
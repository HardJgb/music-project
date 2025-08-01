import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import PlaylistDetail from '../views/PlaylistDetail.vue'
import UserProfile from '@/views/UserProfile.vue'
import FavoriteMusic from '../views/FavoriteMusic.vue'
import FavoritePlaylistView from '../views/FavoritePlaylistView.vue'
import SongListView from '../views/SongListView.vue'
import RankingView from '../views/RankingView.vue'
import CategoryView from '../views/CategoryView.vue'
import ArtistsView from '../views/ArtistsView.vue'
import ArtistDetail from '../views/ArtistDetail.vue'
import SongDetail from '../views/SongDetail.vue'
import { useUserStore } from '@/stores/userStore'
import { ElMessage } from 'element-plus'
import AdminLyrics from '../views/AdminLyrics.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/playlist/:id',
    name: 'playlistDetail',
    component: PlaylistDetail
  },
  {
    path: '/songs',
    name: 'songList',
    component: SongListView,
    meta: {
      title: '歌单'
    }
  },
  {
    path: '/ranking',
    name: 'ranking',
    component: RankingView,
    meta: {
      title: '排行榜'
    }
  },
  {
    path: '/about',
    name: 'about',
    // route level code-splitting
    // this generates a separate chunk (About.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import('../views/AboutView.vue'),
  },
  {
    path: '/user/profile',
    name: 'userProfile',
    component: UserProfile,
    meta: {
      requiresAuth: true,
      title: '个人中心'
    }
  },
  {
    path: '/favorite-music',
    name: 'favorite-music',
    component: FavoriteMusic,
    meta: { requiresAuth: true }
  },
  {
    path: '/favorite-playlist',
    name: 'favorite-playlist',
    component: FavoritePlaylistView,
    meta: { 
      requiresAuth: true,
      title: '收藏的歌单'
    }
  },
  {
    path: '/categories',
    name: 'categories',
    component: CategoryView,
    meta: {
      title: '歌单分类'
    }
  },
  {
    path: '/artists',
    name: 'artists',
    component: ArtistsView,
    meta: {
      title: '歌手'
    }
  },
  {
    path: '/artist/:artist',
    name: 'artistDetail',
    component: ArtistDetail,
    meta: {
      title: '歌手详情'
    }
  },
  {
    path: '/song/:id',
    name: 'songDetail',
    component: SongDetail,
    meta: {
      title: '歌曲详情'
    }
  },
  {
    path: '/latest',
    name: 'latestMusic',
    component: () => import('../views/LatestView.vue'),
    meta: {
      title: '最新音乐'
    }
  },
  {
    path: '/admin/lyrics',
    name: 'AdminLyrics',
    component: AdminLyrics,
    meta: {
      title: '歌词管理'
    }
  },
  {
    path: '/recent',
    name: 'recentPlay',
    component: () => import('../views/RecentPlayView.vue'),
    meta: {
      requiresAuth: true,
      title: '最近播放'
    }
  },
  {
    path: '/my-playlists',
    name: 'myPlaylists',
    component: () => import('../views/MyPlaylistsView.vue'),
    meta: {
      requiresAuth: true,
      title: '我的歌单'
    }
  },
  {
    path: '/user-playlist/:id',
    name: 'userPlaylistDetail',
    component: () => import('../views/UserPlaylistDetail.vue'),
    meta: {
      requiresAuth: true,
      title: '歌单详情'
    }
  },
  {
    path: '/album/:id',
    name: 'albumDetail',
    component: () => import('../views/AlbumDetail.vue'),
    meta: {
      title: '专辑详情'
    }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL || '/'),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 如果路由需要登录但用户未登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    next('/login')
  } else {
    // 设置页面标题
    if (to.meta.title) {
      document.title = to.meta.title + ' - 音乐播放器'
    } else {
      document.title = '音乐播放器'
    }
    next()
  }
})

export default router

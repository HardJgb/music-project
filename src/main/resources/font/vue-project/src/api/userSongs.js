import axios from 'axios'
import { API_BASE_URL } from './config'

// 添加请求拦截器，自动添加token
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.token = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

/**
 * 获取用户最常播放的5首歌曲
 * @returns {Promise}
 */
export function getMostPlayedSongs() {
  return axios.get(`${API_BASE_URL}/api/user-songs/most-played`)
}

/**
 * 记录用户播放歌曲
 * @param {number} songId - 歌曲ID
 * @returns {Promise}
 */
export function recordSongPlay(songId) {
  return axios.post(`${API_BASE_URL}/api/user-songs/play?songId=${songId}`)
} 
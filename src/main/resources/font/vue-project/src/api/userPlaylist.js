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
 * 创建歌单
 * @param {Object} playlist - 歌单信息
 * @param {string} playlist.name - 歌单名称
 * @param {string} playlist.description - 歌单描述
 * @param {boolean} playlist.isPrivate - 是否为私密歌单
 * @param {File} playlist.coverImage - 歌单封面图片
 * @returns {Promise}
 */
export function createUserPlaylist(playlist) {
  const formData = new FormData()
  formData.append('name', playlist.name)
  
  if (playlist.description) {
    formData.append('description', playlist.description)
  }
  
  if (playlist.isPrivate !== undefined) {
    formData.append('isPrivate', playlist.isPrivate)
  }
  
  if (playlist.coverImage) {
    formData.append('coverImage', playlist.coverImage)
  }
  
  return axios.post(`${API_BASE_URL}/api/user-playlists/create`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取用户创建的歌单列表
 * @returns {Promise}
 */
export function getUserPlaylists() {
  return axios.get(`${API_BASE_URL}/api/user-playlists/list`)
}

/**
 * 获取歌单详情
 * @param {number} id - 歌单ID
 * @returns {Promise}
 */
export function getUserPlaylistDetail(id) {
  return axios.get(`${API_BASE_URL}/api/user-playlists/${id}`)
}

/**
 * 更新歌单信息
 * @param {number} id - 歌单ID
 * @param {Object} playlist - 歌单信息
 * @returns {Promise}
 */
export function updateUserPlaylist(id, playlist) {
  const formData = new FormData()
  
  if (playlist.name) {
    formData.append('name', playlist.name)
  }
  
  if (playlist.description !== undefined) {
    formData.append('description', playlist.description)
  }
  
  if (playlist.isPrivate !== undefined) {
    formData.append('isPrivate', playlist.isPrivate)
  }
  
  if (playlist.coverImage) {
    formData.append('coverImage', playlist.coverImage)
  }
  
  return axios.put(`${API_BASE_URL}/api/user-playlists/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除歌单
 * @param {number} id - 歌单ID
 * @returns {Promise}
 */
export function deleteUserPlaylist(id) {
  return axios.delete(`${API_BASE_URL}/api/user-playlists/${id}`)
}

/**
 * 添加歌曲到歌单
 * @param {number} playlistId - 歌单ID
 * @param {number} songId - 歌曲ID
 * @returns {Promise}
 */
export function addSongToUserPlaylist(playlistId, songId) {
  return axios.post(`${API_BASE_URL}/api/user-playlists/${playlistId}/songs?songId=${songId}`)
}

/**
 * 从歌单中移除歌曲
 * @param {number} playlistId - 歌单ID
 * @param {number} songId - 歌曲ID
 * @returns {Promise}
 */
export function removeSongFromUserPlaylist(playlistId, songId) {
  return axios.delete(`${API_BASE_URL}/api/user-playlists/${playlistId}/songs?songId=${songId}`)
} 
import axios from 'axios';
import { API_BASE_URL } from './config';

// 添加请求拦截器，统一处理 token
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.token = token;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 收藏歌单
export const addFavoritePlaylist = async (playlistId) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/favorite-playlist/add`, null, {
      params: { playlistId }
    });
    return response.data;
  } catch (error) {
    console.error('收藏歌单失败:', error);
    throw error;
  }
};

// 取消收藏歌单
export const removeFavoritePlaylist = async (playlistId) => {
  try {
    const response = await axios.delete(`${API_BASE_URL}/api/favorite-playlist/remove`, {
      params: { playlistId }
    });
    return response.data;
  } catch (error) {
    console.error('取消收藏失败:', error);
    throw error;
  }
};

// 获取收藏的歌单列表
export const getFavoritePlaylists = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/favorite-playlist/list`);
    return response.data;
  } catch (error) {
    console.error('获取收藏歌单失败:', error);
    throw error;
  }
};

// 检查是否已收藏歌单
export const checkFavorite = async (playlistId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/favorite-playlist/check`, {
      params: { playlistId }
    });
    return response.data;
  } catch (error) {
    console.error('检查收藏状态失败:', error);
    throw error;
  }
}; 
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

// 收藏歌曲
export const addFavoriteSong = async (songId) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/favorite-song/add`, null, {
      params: { songId }
    });
    return response.data;
  } catch (error) {
    console.error('收藏歌曲失败:', error);
    throw error;
  }
};

// 取消收藏歌曲
export const removeFavoriteSong = async (songId) => {
  try {
    const response = await axios.delete(`${API_BASE_URL}/api/favorite-song/remove`, {
      params: { songId }
    });
    return response.data;
  } catch (error) {
    console.error('取消收藏歌曲失败:', error);
    throw error;
  }
};

// 获取收藏的歌曲列表
export const getFavoriteSongs = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/favorite-song/list`);
    return response.data;
  } catch (error) {
    console.error('获取收藏歌曲失败:', error);
    throw error;
  }
};

// 检查是否已收藏歌曲
export const checkFavoriteSong = async (songId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/favorite-song/check`, {
      params: { songId }
    });
    return response.data;
  } catch (error) {
    console.error('检查歌曲收藏状态失败:', error);
    throw error;
  }
}; 
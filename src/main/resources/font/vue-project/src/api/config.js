// API配置文件
export const API_BASE_URL = 'http://localhost:8085';

// 歌单相关API
export const PLAYLIST_API = {
  getAllPlaylists: `${API_BASE_URL}/api/playlists`,
  getRecommendedPlaylists: `${API_BASE_URL}/api/playlists/recommended`,
  getPlaylistById: (id) => `${API_BASE_URL}/api/playlists/${id}`,
  createPlaylist: `${API_BASE_URL}/api/playlists`,
  updatePlaylist: (id) => `${API_BASE_URL}/api/playlists/${id}`,
  deletePlaylist: (id) => `${API_BASE_URL}/api/playlists/${id}`
};

// 歌曲相关API
export const SONG_API = {
  getAllSongs: `${API_BASE_URL}/api/songs`,
  getSongById: (id) => `${API_BASE_URL}/api/songs/${id}`,
  getSongsByPlaylistId: (playlistId) => `${API_BASE_URL}/api/songs/playlist/${playlistId}`,
  createSong: `${API_BASE_URL}/api/songs`,
  updateSong: (id) => `${API_BASE_URL}/api/songs/${id}`,
  deleteSong: (id) => `${API_BASE_URL}/api/songs/${id}`,
  incrementPlayCount: (id) => `${API_BASE_URL}/api/songs/${id}/play`,
  getRanking: `${API_BASE_URL}/api/songs/ranking`
};

// 获取图片的完整URL
export const getImageUrl = (path) => {
  if (!path) return `${API_BASE_URL}/public/image/default-cover.jpg`
  
  // 如果已经是完整URL，直接返回
  if (path.startsWith('http')) {
    return path
  }
  
  // 确保路径包含正确的前缀
  if (!path.includes('/public/image/')) {
    path = '/public/image/' + path
  }
  
  // 生成完整URL并返回
  const url = `${API_BASE_URL}${path.startsWith('/') ? '' : '/'}${path}`
  console.log(`生成图片URL: ${path} -> ${url}`)
  return url
}

// 添加专门处理音频URL的函数
export const getAudioUrl = (path) => {
  if (!path) {
    console.error('音频路径为空')
    // 设置一个默认的测试音频
    return 'http://localhost:8085/public/image/日落大道.mp3'
  }
  
  if (path.startsWith('http')) {
    return path;
  } else {
    const fullUrl = `${API_BASE_URL}${path.startsWith('/') ? '' : '/'}${path}`
    console.log('生成的音频URL:', fullUrl)
    return fullUrl;
  }
}; 
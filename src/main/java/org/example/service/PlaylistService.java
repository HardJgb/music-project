package org.example.service;

import org.example.mapper.PlaylistMapper;
import org.example.mapper.PlaylistSongMapper;
import org.example.mapper.SongMapper;
import org.example.pojo.Playlist;
import org.example.pojo.PlaylistSong;
import org.example.pojo.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlaylistService {
    
    @Autowired
    private PlaylistMapper playlistMapper;
    
    @Autowired
    private SongMapper songMapper;
    
    @Autowired
    private PlaylistSongMapper playlistSongMapper;
    
    public List<Playlist> getAllPlaylists() {
        List<Playlist> playlists = playlistMapper.findAll();
        // 为每个歌单加载其包含的歌曲
        for (Playlist playlist : playlists) {
            List<Song> songs = songMapper.findByPlaylistId(playlist.getId());
            playlist.setSongs(songs);
        }
        return playlists;
    }
    
    public List<Playlist> getRecommendedPlaylists() {
        List<Playlist> playlists = playlistMapper.findRecommendedPlaylists();
        // 为每个推荐歌单加载其包含的歌曲
        for (Playlist playlist : playlists) {
            List<Song> songs = songMapper.findByPlaylistId(playlist.getId());
            playlist.setSongs(songs);
        }
        return playlists;
    }
    
    public Playlist getPlaylistById(Integer id) {
        Playlist playlist = playlistMapper.findById(id);
        if (playlist != null) {
            List<Song> songs = songMapper.findByPlaylistId(playlist.getId());
            playlist.setSongs(songs);
        }
        return playlist;
    }
    
    public int createPlaylist(Playlist playlist) {
        return playlistMapper.insert(playlist);
    }
    
    public int updatePlaylist(Playlist playlist) {
        return playlistMapper.update(playlist);
    }
    
    public int deletePlaylist(Integer id) {
        return playlistMapper.delete(id);
    }
    
    /**
     * 根据分类获取歌单列表
     */
    public List<Playlist> getPlaylistsByCategory(String category) {
        List<Playlist> playlists = playlistMapper.findByCategory(category);
        // 为每个歌单加载其包含的歌曲
        for (Playlist playlist : playlists) {
            List<Song> songs = songMapper.findByPlaylistId(playlist.getId());
            playlist.setSongs(songs);
        }
        return playlists;
    }
    
    /**
     * 获取所有歌单分类
     */
    public List<String> getAllCategories() {
        return playlistMapper.findAllCategories();
    }
    
    /**
     * 添加歌曲到歌单
     * @param playlistId 歌单ID
     * @param songId 歌曲ID
     * @return 影响的行数
     */
    @Transactional
    public int addSongToPlaylist(Integer playlistId, Integer songId) {
        // 检查歌单是否存在
        Playlist playlist = playlistMapper.findById(playlistId);
        if (playlist == null) {
            throw new RuntimeException("歌单不存在");
        }
        
        // 检查歌曲是否存在
        Song song = songMapper.findById(songId);
        if (song == null) {
            throw new RuntimeException("歌曲不存在");
        }
        
        // 检查歌曲是否已在歌单中
        List<PlaylistSong> existingSongs = playlistSongMapper.findByPlaylistId(playlistId);
        for (PlaylistSong ps : existingSongs) {
            if (ps.getSongId().equals(songId)) {
                return 0; // 歌曲已存在于歌单中，不需要重复添加
            }
        }
        
        // 添加歌曲到歌单
        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setPlaylistId(playlistId);
        playlistSong.setSongId(songId);
        return playlistSongMapper.insert(playlistSong);
    }
    
    /**
     * 从歌单中删除歌曲
     * @param playlistId 歌单ID
     * @param songId 歌曲ID
     * @return 影响的行数
     */
    @Transactional
    public int removeSongFromPlaylist(Integer playlistId, Integer songId) {
        // 检查歌单是否存在
        Playlist playlist = playlistMapper.findById(playlistId);
        if (playlist == null) {
            throw new RuntimeException("歌单不存在");
        }
        
        return playlistSongMapper.deleteByPlaylistIdAndSongId(playlistId, songId);
    }
    
    /**
     * 批量添加歌曲到歌单
     * @param playlistId 歌单ID
     * @param songIds 歌曲ID列表
     * @return 成功添加的歌曲数量
     */
    @Transactional
    public int addSongsToPlaylist(Integer playlistId, List<Integer> songIds) {
        // 检查歌单是否存在
        Playlist playlist = playlistMapper.findById(playlistId);
        if (playlist == null) {
            throw new RuntimeException("歌单不存在");
        }
        
        int successCount = 0;
        for (Integer songId : songIds) {
            try {
                int result = addSongToPlaylist(playlistId, songId);
                if (result > 0) {
                    successCount++;
                }
            } catch (Exception e) {
                // 记录错误但继续处理其他歌曲
                System.err.println("添加歌曲 " + songId + " 失败: " + e.getMessage());
            }
        }
        
        return successCount;
    }
    
    /**
     * 清空歌单中的所有歌曲
     * @param playlistId 歌单ID
     * @return 影响的行数
     */
    @Transactional
    public int clearPlaylistSongs(Integer playlistId) {
        // 检查歌单是否存在
        Playlist playlist = playlistMapper.findById(playlistId);
        if (playlist == null) {
            throw new RuntimeException("歌单不存在");
        }
        
        // 从关联表中删除所有相关记录
        List<PlaylistSong> songs = playlistSongMapper.findByPlaylistId(playlistId);
        int count = 0;
        
        for (PlaylistSong song : songs) {
            int result = playlistSongMapper.delete(song.getId());
            count += result;
        }
        
        return count;
    }
} 
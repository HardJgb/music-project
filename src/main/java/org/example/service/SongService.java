package org.example.service;

import org.example.pojo.Song;
import org.example.utils.Result;

import java.util.List;

public interface SongService {
    
    List<Song> getAllSongs();
    
    Song getSongById(Integer id);
    
    List<Song> getSongsByPlaylistId(Integer playlistId);
    
    int createSong(Song song);
    
    int updateSong(Song song);
    
    int deleteSong(Integer id);
    
    int incrementPlayCount(Integer id);
    
    /**
     * 获取按播放次数排序的歌曲列表
     * @return 按播放次数降序排序的歌曲列表
     */
    List<Song> getSongsByPlayCount();

    /**
     * 获取最新歌曲列表
     * @return 按创建时间降序排序的歌曲列表
     */
    List<Song> getLatestSongs();

    /**
     * 获取所有歌手列表
     * @return 不重复的歌手名称列表
     */
    List<String> getAllArtists();

    /**
     * 根据歌手名称获取歌曲
     * @param artist 歌手名称
     * @return 该歌手的所有歌曲
     */
    List<Song> getSongsByArtist(String artist);

    /**
     * 分页查询歌曲
     * @param page 页码
     * @param pageSize 每页数量
     * @param query 查询关键字
     * @return 分页结果
     */
    Result getSongsByPage(int page, int pageSize, String query);
} 
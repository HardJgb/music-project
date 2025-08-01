package org.example.service;

import org.example.pojo.Album;
import org.example.pojo.Song;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AlbumService {
    /**
     * 获取所有专辑
     */
    List<Album> getAllAlbums();
    
    /**
     * 根据ID获取专辑详情（包含歌曲列表）
     */
    Album getAlbumById(Integer id);
    
    /**
     * 根据艺术家名称获取专辑列表
     */
    List<Album> getAlbumsByArtist(String artist);
    
    /**
     * 根据专辑名称搜索专辑
     */
    List<Album> searchAlbumsByName(String name);
    
    /**
     * 获取最新发布的专辑
     */
    List<Album> getLatestAlbums();
    
    /**
     * 添加新专辑
     */
    int addAlbum(Album album);
    
    /**
     * 更新专辑信息
     */
    int updateAlbum(Album album);
    
    /**
     * 删除专辑
     */
    int deleteAlbum(Integer id);
    
    /**
     * 添加歌曲到专辑
     */
    int addSongToAlbum(Integer albumId, Integer songId, Integer trackNumber);
    
    /**
     * 从专辑中移除歌曲
     */
    int removeSongFromAlbum(Integer albumId, Integer songId);
    
    /**
     * 获取专辑中的所有歌曲
     */
    List<Song> getAlbumSongs(Integer albumId);
    
    /**
     * 上传专辑封面
     */
    String uploadAlbumCover(Integer albumId, byte[] fileData, String originalFilename) throws IOException;
    
    /**
     * 导入已有歌手的专辑
     */
    int importExistingAlbums();
    
    /**
     * 更新歌曲在专辑中的曲目编号
     */
    int updateTrackNumber(Integer albumId, Integer songId, Integer trackNumber);
} 
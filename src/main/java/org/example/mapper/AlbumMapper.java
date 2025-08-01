package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pojo.Album;
import org.example.pojo.Song;

import java.util.List;

@Mapper
public interface AlbumMapper {
    /**
     * 获取所有专辑
     */
    List<Album> findAll();
    
    /**
     * 获取最新专辑
     */
    List<Album> findLatest();
    
    /**
     * 根据ID获取专辑
     */
    Album findById(Integer id);
    
    /**
     * 根据艺术家获取专辑
     */
    List<Album> findByArtist(String artist);
    
    /**
     * 根据名称搜索专辑
     */
    List<Album> searchByName(@Param("name") String name);
    
    /**
     * 添加专辑
     */
    int insert(Album album);
    
    /**
     * 更新专辑
     */
    int update(Album album);
    
    /**
     * 删除专辑
     */
    int delete(Integer id);
    
    /**
     * 更新专辑封面
     */
    int updateCover(@Param("id") Integer id, @Param("coverUrl") String coverUrl);
    
    /**
     * 获取专辑中的所有歌曲
     */
    List<Song> getAlbumSongs(Integer albumId);
    
    /**
     * 检查歌曲是否已存在于专辑中
     */
    Integer checkSongInAlbum(@Param("albumId") Integer albumId, @Param("songId") Integer songId);
    
    /**
     * 获取专辑中最高的曲目编号
     */
    Integer getMaxTrackNumber(Integer albumId);
    
    /**
     * 更新专辑歌曲数量
     */
    int updateSongCount(@Param("id") Integer id);
    
    /**
     * 导入已有专辑数据
     */
    int importExistingAlbums();
} 
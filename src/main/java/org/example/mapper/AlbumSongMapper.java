package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pojo.AlbumSong;
import org.example.pojo.Song;

import java.util.List;

@Mapper
public interface AlbumSongMapper {
    /**
     * 添加歌曲到专辑
     */
    int insert(AlbumSong albumSong);
    
    /**
     * 批量添加歌曲到专辑
     */
    int batchInsert(List<AlbumSong> albumSongs);
    
    /**
     * 从专辑中移除歌曲
     */
    int remove(@Param("albumId") Integer albumId, @Param("songId") Integer songId);
    
    /**
     * 获取专辑中的所有歌曲
     */
    List<Song> findSongsByAlbumId(Integer albumId);
    
    /**
     * 更新歌曲在专辑中的曲目编号
     * 该方法支持两种调用方式：
     * 1. 传入AlbumSong对象
     * 2. 传入单独的参数
     */
    int updateTrackNumber(@Param("albumId") Integer albumId, 
                        @Param("songId") Integer songId, 
                        @Param("trackNumber") Integer trackNumber);
    
    /**
     * 删除专辑中的所有歌曲
     */
    int removeAllSongs(Integer albumId);
    
    /**
     * 检查歌曲是否在专辑中
     */
    boolean existsInAlbum(@Param("albumId") Integer albumId, @Param("songId") Integer songId);

    /**
     * 添加歌曲到专辑
     *
     * @param albumId     专辑ID
     * @param songId      歌曲ID
     * @param trackNumber 曲目编号
     * @return 影响的行数
     */
    int addSongToAlbum(@Param("albumId") Integer albumId, 
                       @Param("songId") Integer songId, 
                       @Param("trackNumber") Integer trackNumber);
    
    /**
     * 从专辑中移除歌曲
     *
     * @param albumId 专辑ID
     * @param songId  歌曲ID
     * @return 影响的行数
     */
    int removeSongFromAlbum(@Param("albumId") Integer albumId, 
                           @Param("songId") Integer songId);
    
    /**
     * 获取歌曲在专辑中的曲目编号
     *
     * @param albumId 专辑ID
     * @param songId  歌曲ID
     * @return 曲目编号
     */
    Integer getTrackNumber(@Param("albumId") Integer albumId, 
                          @Param("songId") Integer songId);
} 
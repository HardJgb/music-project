package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pojo.SongLyrics;

/**
 * 歌词数据访问接口
 */
@Mapper
public interface LyricsMapper {
    
    /**
     * 根据歌曲ID查找歌词
     */
    SongLyrics findBySongId(@Param("songId") Integer songId);
    
    /**
     * 添加歌词
     */
    int insert(SongLyrics lyrics);
    
    /**
     * 更新歌词
     */
    int update(SongLyrics lyrics);
    
    /**
     * 删除歌词
     */
    int deleteBySongId(@Param("songId") Integer songId);
} 
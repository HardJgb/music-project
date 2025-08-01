package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.pojo.UserPlaylistSong;

import java.util.List;

@Mapper
public interface UserPlaylistSongMapper extends BaseMapper<UserPlaylistSong> {
    
    /**
     * 查询歌单中的所有歌曲ID
     */
    @Select("SELECT song_id FROM user_playlist_song WHERE user_playlist_id = #{userPlaylistId} ORDER BY create_time DESC")
    List<Integer> findSongIdsByUserPlaylistId(@Param("userPlaylistId") Integer userPlaylistId);
    
    /**
     * 删除歌单中的指定歌曲
     */
    @Delete("DELETE FROM user_playlist_song WHERE user_playlist_id = #{userPlaylistId} AND song_id = #{songId}")
    int deleteByUserPlaylistIdAndSongId(@Param("userPlaylistId") Integer userPlaylistId, @Param("songId") Integer songId);
    
    /**
     * 清空歌单中的所有歌曲
     */
    @Delete("DELETE FROM user_playlist_song WHERE user_playlist_id = #{userPlaylistId}")
    int deleteAllByUserPlaylistId(@Param("userPlaylistId") Integer userPlaylistId);
    
    /**
     * 检查歌曲是否已在歌单中
     */
    @Select("SELECT COUNT(*) FROM user_playlist_song WHERE user_playlist_id = #{userPlaylistId} AND song_id = #{songId}")
    int existsByUserPlaylistIdAndSongId(@Param("userPlaylistId") Integer userPlaylistId, @Param("songId") Integer songId);
} 
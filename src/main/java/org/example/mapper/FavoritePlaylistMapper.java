package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.pojo.FavoritePlaylist;

import java.util.List;

@Mapper
public interface FavoritePlaylistMapper extends BaseMapper<FavoritePlaylist> {
    
    /**
     * 查询用户是否收藏了指定歌单
     */
    @Select("SELECT * FROM favorite_playlist WHERE user_id = #{userId} AND playlist_id = #{playlistId} LIMIT 1")
    FavoritePlaylist findByUserIdAndPlaylistId(@Param("userId") Integer userId, @Param("playlistId") Integer playlistId);
    
    /**
     * 查询用户收藏的所有歌单ID
     */
    @Select("SELECT playlist_id FROM favorite_playlist WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Integer> findPlaylistIdsByUserId(@Param("userId") Integer userId);
    
    /**
     * 删除用户收藏的歌单
     */
    @Delete("DELETE FROM favorite_playlist WHERE user_id = #{userId} AND playlist_id = #{playlistId}")
    int deleteByUserIdAndPlaylistId(@Param("userId") Integer userId, @Param("playlistId") Integer playlistId);
} 
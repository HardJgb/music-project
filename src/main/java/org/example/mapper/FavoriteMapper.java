package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.pojo.Favorite;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    
    // 添加收藏
    @Insert("INSERT INTO favorites (user_id, song_id, song_name, artist, album, cover_url) " +
            "VALUES (#{userId}, #{songId}, #{songName}, #{artist}, #{album}, #{coverUrl})")
    int addFavorite(Favorite favorite);
    
    // 取消收藏
    @Delete("DELETE FROM favorites WHERE user_id = #{userId} AND song_id = #{songId}")
    int removeFavorite(@Param("userId") Integer userId, @Param("songId") Integer songId);
    
    // 获取用户的所有收藏
    @Select("SELECT * FROM favorites WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Favorite> getUserFavorites(Integer userId);
    
    // 检查歌曲是否已被收藏
    @Select("SELECT COUNT(*) FROM favorites WHERE user_id = #{userId} AND song_id = #{songId}")
    int checkFavorite(@Param("userId") Integer userId, @Param("songId") Integer songId);
} 
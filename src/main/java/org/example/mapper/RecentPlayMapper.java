package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.pojo.RecentPlay;
import java.util.List;

@Mapper
public interface RecentPlayMapper {
    
    @Insert("INSERT INTO recent_play(user_id, song_id, play_time) VALUES(#{userId}, #{songId}, NOW())")
    int insert(RecentPlay recentPlay);
    
    @Select("SELECT * FROM recent_play WHERE user_id = #{userId} ORDER BY play_time DESC LIMIT #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "songId", column = "song_id"),
        @Result(property = "playTime", column = "play_time"),
        @Result(property = "song", column = "song_id",
                one = @One(select = "org.example.mapper.SongMapper.selectById"))
    })
    List<RecentPlay> selectByUserId(@Param("userId") Integer userId, @Param("limit") Integer limit);
    
    @Delete("DELETE FROM recent_play WHERE user_id = #{userId} AND song_id = #{songId}")
    int deleteByUserIdAndSongId(@Param("userId") Integer userId, @Param("songId") Integer songId);
    
    @Delete("DELETE FROM recent_play WHERE user_id = #{userId}")
    int deleteByUserId(Integer userId);
} 
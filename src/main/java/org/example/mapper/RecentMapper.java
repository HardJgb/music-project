package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.pojo.Recent;

import java.util.List;

@Mapper
public interface RecentMapper extends BaseMapper<Recent> {
    
    @Select("SELECT * FROM recent_play WHERE user_id = #{userId} AND song_id = #{songId} LIMIT 1")
    Recent findByUserIdAndSongId(@Param("userId") Integer userId, @Param("songId") Integer songId);
    
    @Update("UPDATE recent_play SET play_time = NOW() WHERE user_id = #{userId} AND song_id = #{songId}")
    int updateTimestamp(@Param("userId") Integer userId, @Param("songId") Integer songId);
    
    @Select("SELECT * FROM recent_play WHERE user_id = #{userId} ORDER BY play_time DESC")
    List<Recent> findByUserId(@Param("userId") Integer userId);

    @Delete("DELETE FROM recent_play WHERE user_id = #{userId} AND song_id = #{songId}")
    int deleteByUserIdAndSongId(@Param("userId") Integer userId, @Param("songId") Integer songId);

    @Delete("DELETE FROM recent_play WHERE user_id = #{userId}")
    int deleteAllByUserId(@Param("userId") Integer userId);
} 
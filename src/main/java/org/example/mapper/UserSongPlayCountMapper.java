package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.pojo.UserSongPlayCount;

import java.util.List;

@Mapper
public interface UserSongPlayCountMapper extends BaseMapper<UserSongPlayCount> {
    
    /**
     * 根据用户ID和歌曲ID查询记录
     */
    @Select("SELECT * FROM user_song_play_count WHERE user_id = #{userId} AND song_id = #{songId}")
    UserSongPlayCount findByUserIdAndSongId(@Param("userId") Integer userId, @Param("songId") Integer songId);
    
    /**
     * 查询用户播放最多的N首歌曲
     */
    @Select("SELECT * FROM user_song_play_count WHERE user_id = #{userId} ORDER BY play_count DESC, last_play_time DESC LIMIT #{limit}")
    List<UserSongPlayCount> findMostPlayedByUserId(@Param("userId") Integer userId, @Param("limit") Integer limit);
} 
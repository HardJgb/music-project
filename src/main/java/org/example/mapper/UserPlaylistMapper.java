package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.pojo.UserPlaylist;

import java.util.List;

@Mapper
public interface UserPlaylistMapper extends BaseMapper<UserPlaylist> {
    
    /**
     * 查询用户创建的所有歌单
     */
    @Select("SELECT * FROM user_playlist WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<UserPlaylist> findByUserId(@Param("userId") Integer userId);
    
    /**
     * 查询用户的歌单数量
     */
    @Select("SELECT COUNT(*) FROM user_playlist WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);
} 
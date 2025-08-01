package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pojo.Song;
import java.util.List;

@Mapper
public interface SongMapper extends BaseMapper<Song> {
    List<Song> findAll();
    List<Song> findAllOrderByPlayCount();
    List<Song> findAllOrderByCreateTime();
    Song findById(Integer id);
    List<Song> findByPlaylistId(Integer playlistId);
    int insertSong(Song song);
    int updateSong(Song song);
    int deleteSong(Integer id);
    int incrementPlayCount(Integer id);
    
    // 获取所有不重复的歌手
    List<String> findAllArtists();
    
    // 根据歌手名称查询歌曲
    List<Song> findByArtist(String artist);
    
    // 删除关联表数据
    int deleteRecentPlayBySongId(Integer songId);
    int deleteFavoriteBySongId(Integer songId);
    int deleteAlbumSongBySongId(Integer songId);
    int deleteCommentsBySongId(Integer songId);
} 
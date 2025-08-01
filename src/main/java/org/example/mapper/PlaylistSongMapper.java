package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.pojo.PlaylistSong;
import java.util.List;

@Mapper
public interface PlaylistSongMapper {
    List<PlaylistSong> findByPlaylistId(Integer playlistId);
    int insert(PlaylistSong playlistSong);
    int delete(Integer id);
    int deleteByPlaylistIdAndSongId(Integer playlistId, Integer songId);
} 
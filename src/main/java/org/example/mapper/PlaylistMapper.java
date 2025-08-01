package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.pojo.Playlist;
import java.util.List;

@Mapper
public interface PlaylistMapper {
    List<Playlist> findAll();
    List<Playlist> findRecommendedPlaylists();
    Playlist findById(Integer id);
    List<Playlist> findByCategory(String category);
    List<String> findAllCategories();
    int insert(Playlist playlist);
    int update(Playlist playlist);
    int delete(Integer id);
} 
package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.pojo.Artist;
import java.util.List;

@Mapper
public interface ArtistMapper {
    // 查询所有歌手
    List<Artist> findAll();
    
    // 根据ID查询歌手
    Artist findById(Integer id);
    
    // 根据名称查询歌手
    Artist findByName(String name);
    
    // 添加歌手
    int insert(Artist artist);
    
    // 更新歌手信息
    int update(Artist artist);
    
    // 删除歌手
    int delete(Integer id);
    
    // 导入现有歌手数据 (从song表中提取不重复的歌手名称，并创建对应的歌手记录)
    int importExistingArtists();
} 
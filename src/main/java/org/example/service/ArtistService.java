package org.example.service;

import org.example.pojo.Artist;
import java.util.List;

public interface ArtistService {
    // 获取所有歌手
    List<Artist> getAllArtists();
    
    // 根据ID获取歌手
    Artist getArtistById(Integer id);
    
    // 根据名称获取歌手
    Artist getArtistByName(String name);
    
    // 添加歌手
    int addArtist(Artist artist);
    
    // 更新歌手信息
    int updateArtist(Artist artist);
    
    // 删除歌手
    int deleteArtist(Integer id);
    
    // 导入现有歌手
    int importExistingArtists();
    
    // 上传歌手头像
    String uploadArtistAvatar(Integer artistId, byte[] fileData, String fileName);
} 
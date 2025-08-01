package org.example.service.impl;

import org.example.mapper.ArtistMapper;
import org.example.pojo.Artist;
import org.example.service.ArtistService;
import org.example.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ArtistServiceImpl implements ArtistService {

    @Autowired
    private ArtistMapper artistMapper;
    
    @Value("${imageUpload.basePath}")
    private String uploadPath;

    @Value("${imageUpload.accessPath}")
    private String accessPath;

    @Override
    public List<Artist> getAllArtists() {
        return artistMapper.findAll();
    }

    @Override
    public Artist getArtistById(Integer id) {
        return artistMapper.findById(id);
    }

    @Override
    public Artist getArtistByName(String name) {
        return artistMapper.findByName(name);
    }

    @Override
    @Transactional
    public int addArtist(Artist artist) {
        return artistMapper.insert(artist);
    }

    @Override
    @Transactional
    public int updateArtist(Artist artist) {
        return artistMapper.update(artist);
    }

    @Override
    @Transactional
    public int deleteArtist(Integer id) {
        return artistMapper.delete(id);
    }

    @Override
    @Transactional
    public int importExistingArtists() {
        return artistMapper.importExistingArtists();
    }

    @Override
    @Transactional
    public String uploadArtistAvatar(Integer artistId, byte[] fileData, String fileName) {
        Artist artist = artistMapper.findById(artistId);
        if (artist == null) {
            throw new RuntimeException("歌手不存在");
        }
        
        try {
            // 确保目录存在
            String artistImagesDir = uploadPath + "artists/";
            File dir = new File(artistImagesDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
            }
            
            // 生成文件名
            String extension = FileUtils.getFileExtension(fileName);
            String newFileName = "artist_" + artistId + "_" + UUID.randomUUID().toString() + "." + extension;
            
            // 保存文件
            Path path = Paths.get(artistImagesDir, newFileName);
            Files.write(path, fileData);
            
            // 更新歌手头像URL
            // 从accessPath中提取基础URL
            String baseUrl = accessPath.substring(0, accessPath.lastIndexOf("/public/image/") + "/public/image/".length());
            String avatarUrl = baseUrl + "artists/" + newFileName;
            artist.setAvatarUrl(avatarUrl);
            artistMapper.update(artist);
            
            return avatarUrl;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("上传歌手头像失败: " + e.getMessage(), e);
        }
    }
} 
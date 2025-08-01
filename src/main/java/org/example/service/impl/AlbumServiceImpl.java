package org.example.service.impl;

import org.example.mapper.AlbumMapper;
import org.example.mapper.AlbumSongMapper;
import org.example.pojo.Album;
import org.example.pojo.AlbumSong;
import org.example.pojo.Song;
import org.example.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AlbumServiceImpl implements AlbumService {

    @Autowired
    private AlbumMapper albumMapper;
    
    @Autowired
    private AlbumSongMapper albumSongMapper;
    
    @Value("${file.upload.path:/public/image/}")
    private String fileUploadPath;
    
    @Override
    public List<Album> getAllAlbums() {
        return albumMapper.findAll();
    }
    
    @Override
    public Album getAlbumById(Integer id) {
        Album album = albumMapper.findById(id);
        if (album != null) {
            // 获取专辑中的歌曲
            album.setSongs(albumSongMapper.findSongsByAlbumId(id));
        }
        return album;
    }
    
    @Override
    public List<Album> getAlbumsByArtist(String artist) {
        List<Album> albums = albumMapper.findByArtist(artist);
        return albums;
    }
    
    @Override
    public List<Album> searchAlbumsByName(String name) {
        return albumMapper.searchByName(name);
    }
    
    @Override
    public List<Album> getLatestAlbums() {
        return albumMapper.findLatest();
    }
    
    @Override
    @Transactional
    public int addAlbum(Album album) {
        return albumMapper.insert(album);
    }
    
    @Override
    @Transactional
    public int updateAlbum(Album album) {
        return albumMapper.update(album);
    }
    
    @Override
    @Transactional
    public int deleteAlbum(Integer id) {
        // 先删除专辑与歌曲的关联
        albumSongMapper.removeAllSongs(id);
        // 再删除专辑
        return albumMapper.delete(id);
    }
    
    @Override
    @Transactional
    public int addSongToAlbum(Integer albumId, Integer songId, Integer trackNumber) {
        // 检查歌曲是否已在专辑中
        if (albumSongMapper.existsInAlbum(albumId, songId)) {
            // 如果已存在，只更新曲目编号
            return albumSongMapper.updateTrackNumber(albumId, songId, trackNumber);
        } else {
            // 如果不存在，添加关联
            AlbumSong albumSong = new AlbumSong(albumId, songId, trackNumber);
            return albumSongMapper.insert(albumSong);
        }
    }
    
    @Override
    @Transactional
    public int removeSongFromAlbum(Integer albumId, Integer songId) {
        return albumSongMapper.remove(albumId, songId);
    }
    
    @Override
    public List<Song> getAlbumSongs(Integer albumId) {
        return albumSongMapper.findSongsByAlbumId(albumId);
    }
    
    @Override
    public String uploadAlbumCover(Integer albumId, byte[] fileData, String originalFilename) throws IOException {
        // 确保目录存在
        File uploadDir = new File(fileUploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 生成文件名
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = "album_" + albumId + "_" + UUID.randomUUID().toString().replace("-", "") + fileExtension;
        String filePath = fileUploadPath + fileName;
        
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(fileData);
        }
        
        // 更新专辑封面URL
        albumMapper.updateCover(albumId, fileName);
        
        return fileName;
    }
    
    @Override
    @Transactional
    public int importExistingAlbums() {
        return albumMapper.importExistingAlbums();
    }
    
    @Override
    @Transactional
    public int updateTrackNumber(Integer albumId, Integer songId, Integer trackNumber) {
        // 检查歌曲是否存在于专辑中
        if (!albumSongMapper.existsInAlbum(albumId, songId)) {
            return 0; // 歌曲不在专辑中，返回0表示更新失败
        }
        // 更新曲目编号
        return albumSongMapper.updateTrackNumber(albumId, songId, trackNumber);
    }
} 
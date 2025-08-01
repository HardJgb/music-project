package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.mapper.SongMapper;
import org.example.mapper.UserPlaylistMapper;
import org.example.mapper.UserPlaylistSongMapper;
import org.example.pojo.Song;
import org.example.pojo.UserPlaylist;
import org.example.pojo.UserPlaylistSong;
import org.example.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserPlaylistService {
    
    private static final Logger log = LoggerFactory.getLogger(UserPlaylistService.class);
    
    @Value("${imageUpload.basePath}")
    private String baseUploadPath;
    
    @Value("${imageUpload.accessPath}")
    private String accessPath;
    
    @Autowired
    private UserPlaylistMapper userPlaylistMapper;
    
    @Autowired
    private UserPlaylistSongMapper userPlaylistSongMapper;
    
    @Autowired
    private SongMapper songMapper;
    
    /**
     * 创建歌单
     */
    public Result createPlaylist(String name, String description, Boolean isPrivate, Integer userId, MultipartFile coverImage) {
        try {
            // 检查歌单名称
            if (name == null || name.trim().isEmpty()) {
                return Result.error("歌单名称不能为空");
            }
            
            // 创建歌单
            UserPlaylist userPlaylist = new UserPlaylist();
            userPlaylist.setUserId(userId);
            userPlaylist.setName(name);
            userPlaylist.setDescription(description);
            userPlaylist.setIsPrivate(isPrivate);
            
            // 处理封面图片
            if (coverImage != null && !coverImage.isEmpty()) {
                String filename = UUID.randomUUID().toString() + getFileExtension(coverImage.getOriginalFilename());
                // 使用配置的基础路径
                String uploadDir = baseUploadPath + "playlist-covers\\";
                try {
                    // 确保目录存在
                    File directory = new File(uploadDir);
                    if (!directory.exists()) {
                        boolean created = directory.mkdirs();
                        if (!created) {
                            log.error("无法创建目录: {}", uploadDir);
                            return Result.error("无法创建文件上传目录");
                        }
                    }
                    
                    // 保存文件
                    File destFile = new File(directory, filename);
                    coverImage.transferTo(destFile);
                    log.info("文件已保存到: {}", destFile.getAbsolutePath());
                    
                    // 设置封面路径 - 使用配置的访问路径
                    userPlaylist.setCoverImg(accessPath.replace("http://localhost:8085", "") + "playlist-covers/" + filename);
                } catch (IOException e) {
                    log.error("封面图片上传失败", e);
                    return Result.error("封面图片上传失败: " + e.getMessage());
                }
            }
            
            // 保存歌单
            userPlaylistMapper.insert(userPlaylist);
            
            return Result.success(userPlaylist);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建歌单失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户创建的歌单列表
     */
    public Result getUserPlaylists(Integer userId) {
        try {
            List<UserPlaylist> playlists = userPlaylistMapper.findByUserId(userId);
            return Result.success(playlists);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取歌单列表失败");
        }
    }
    
    /**
     * 获取歌单详情
     */
    public Result getPlaylistDetails(Integer playlistId, Integer userId) {
        try {
            UserPlaylist playlist = userPlaylistMapper.selectById(playlistId);
            if (playlist == null) {
                return Result.error("歌单不存在");
            }
            
            // 检查权限（私密歌单只有创建者可以访问）
            if (playlist.getIsPrivate() && !playlist.getUserId().equals(userId)) {
                return Result.error("没有权限访问此歌单");
            }
            
            // 获取歌单中的歌曲
            List<Integer> songIds = userPlaylistSongMapper.findSongIdsByUserPlaylistId(playlistId);
            List<Song> songs = new ArrayList<>();
            
            for (Integer songId : songIds) {
                Song song = songMapper.selectById(songId);
                if (song != null) {
                    songs.add(song);
                }
            }
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("playlist", playlist);
            result.put("songs", songs);
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取歌单详情失败");
        }
    }
    
    /**
     * 添加歌曲到歌单
     */
    @Transactional
    public Result addSongToPlaylist(Integer playlistId, Integer songId, Integer userId) {
        try {
            // 检查歌单是否存在且属于该用户
            UserPlaylist playlist = userPlaylistMapper.selectById(playlistId);
            if (playlist == null) {
                return Result.error("歌单不存在");
            }
            
            if (!playlist.getUserId().equals(userId)) {
                return Result.error("没有权限操作此歌单");
            }
            
            // 检查歌曲是否存在
            Song song = songMapper.selectById(songId);
            if (song == null) {
                return Result.error("歌曲不存在");
            }
            
            // 检查歌曲是否已在歌单中
            if (userPlaylistSongMapper.existsByUserPlaylistIdAndSongId(playlistId, songId) > 0) {
                return Result.error("歌曲已在歌单中");
            }
            
            // 添加歌曲到歌单
            UserPlaylistSong userPlaylistSong = new UserPlaylistSong();
            userPlaylistSong.setUserPlaylistId(playlistId);
            userPlaylistSong.setSongId(songId);
            userPlaylistSong.setCreateTime(LocalDateTime.now());
            
            userPlaylistSongMapper.insert(userPlaylistSong);
            
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加歌曲到歌单失败");
        }
    }
    
    /**
     * 从歌单中移除歌曲
     */
    @Transactional
    public Result removeSongFromPlaylist(Integer playlistId, Integer songId, Integer userId) {
        try {
            // 检查歌单是否存在且属于该用户
            UserPlaylist playlist = userPlaylistMapper.selectById(playlistId);
            if (playlist == null) {
                return Result.error("歌单不存在");
            }
            
            if (!playlist.getUserId().equals(userId)) {
                return Result.error("没有权限操作此歌单");
            }
            
            // 从歌单中移除歌曲
            int rows = userPlaylistSongMapper.deleteByUserPlaylistIdAndSongId(playlistId, songId);
            
            if (rows > 0) {
                return Result.success(null);
            } else {
                return Result.error("歌曲不在歌单中");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("从歌单中移除歌曲失败");
        }
    }
    
    /**
     * 更新歌单信息
     */
    public Result updatePlaylist(Integer playlistId, String name, String description, Boolean isPrivate, Integer userId, MultipartFile coverImage) {
        try {
            // 检查歌单是否存在且属于该用户
            UserPlaylist playlist = userPlaylistMapper.selectById(playlistId);
            if (playlist == null) {
                return Result.error("歌单不存在");
            }
            
            if (!playlist.getUserId().equals(userId)) {
                return Result.error("没有权限操作此歌单");
            }
            
            // 更新歌单信息
            if (name != null && !name.trim().isEmpty()) {
                playlist.setName(name);
            }
            
            if (description != null) {
                playlist.setDescription(description);
            }
            
            if (isPrivate != null) {
                playlist.setIsPrivate(isPrivate);
            }
            
            // 处理封面图片
            if (coverImage != null && !coverImage.isEmpty()) {
                String filename = UUID.randomUUID().toString() + getFileExtension(coverImage.getOriginalFilename());
                // 使用配置的基础路径
                String uploadDir = baseUploadPath + "playlist-covers\\";
                try {
                    // 确保目录存在
                    File directory = new File(uploadDir);
                    if (!directory.exists()) {
                        boolean created = directory.mkdirs();
                        if (!created) {
                            log.error("无法创建目录: {}", uploadDir);
                            return Result.error("无法创建文件上传目录");
                        }
                    }
                    
                    // 保存文件
                    File destFile = new File(directory, filename);
                    coverImage.transferTo(destFile);
                    log.info("文件已保存到: {}", destFile.getAbsolutePath());
                    
                    // 设置封面路径 - 使用配置的访问路径
                    playlist.setCoverImg(accessPath.replace("http://localhost:8085", "") + "playlist-covers/" + filename);
                } catch (IOException e) {
                    log.error("封面图片上传失败", e);
                    return Result.error("封面图片上传失败: " + e.getMessage());
                }
            }
            
            // 保存修改
            userPlaylistMapper.updateById(playlist);
            
            return Result.success(playlist);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新歌单失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除歌单
     */
    @Transactional
    public Result deletePlaylist(Integer playlistId, Integer userId) {
        try {
            // 检查歌单是否存在且属于该用户
            UserPlaylist playlist = userPlaylistMapper.selectById(playlistId);
            if (playlist == null) {
                return Result.error("歌单不存在");
            }
            
            if (!playlist.getUserId().equals(userId)) {
                return Result.error("没有权限操作此歌单");
            }
            
            // 先删除歌单中的所有歌曲
            userPlaylistSongMapper.deleteAllByUserPlaylistId(playlistId);
            
            // 再删除歌单
            userPlaylistMapper.deleteById(playlistId);
            
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除歌单失败");
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf);
    }
} 
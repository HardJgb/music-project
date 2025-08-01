package org.example.controller;

import org.example.service.UserPlaylistService;
import org.example.utils.JwtHelper;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user-playlists")
public class UserPlaylistController {
    
    @Autowired
    private UserPlaylistService userPlaylistService;
    
    @Autowired
    private JwtHelper jwtHelper;
    
    /**
     * 创建歌单
     */
    @PostMapping("/create")
    public Result createPlaylist(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "false") Boolean isPrivate,
            @RequestParam(required = false) MultipartFile coverImage,
            @RequestHeader(value = "token", required = false) String token) {
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return userPlaylistService.createPlaylist(name, description, isPrivate, userId, coverImage);
    }
    
    /**
     * 获取用户创建的歌单列表
     */
    @GetMapping("/list")
    public Result getUserPlaylists(@RequestHeader(value = "token", required = false) String token) {
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return userPlaylistService.getUserPlaylists(userId);
    }
    
    /**
     * 获取歌单详情
     */
    @GetMapping("/{id}")
    public Result getPlaylistDetails(
            @PathVariable("id") Integer playlistId,
            @RequestHeader(value = "token", required = false) String token) {
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return userPlaylistService.getPlaylistDetails(playlistId, userId);
    }
    
    /**
     * 添加歌曲到歌单
     */
    @PostMapping("/{id}/songs")
    public Result addSongToPlaylist(
            @PathVariable("id") Integer playlistId,
            @RequestParam Integer songId,
            @RequestHeader(value = "token", required = false) String token) {
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return userPlaylistService.addSongToPlaylist(playlistId, songId, userId);
    }
    
    /**
     * 从歌单中移除歌曲
     */
    @DeleteMapping("/{id}/songs")
    public Result removeSongFromPlaylist(
            @PathVariable("id") Integer playlistId,
            @RequestParam Integer songId,
            @RequestHeader(value = "token", required = false) String token) {
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return userPlaylistService.removeSongFromPlaylist(playlistId, songId, userId);
    }
    
    /**
     * 更新歌单信息
     */
    @PutMapping("/{id}")
    public Result updatePlaylist(
            @PathVariable("id") Integer playlistId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isPrivate,
            @RequestParam(required = false) MultipartFile coverImage,
            @RequestHeader(value = "token", required = false) String token) {
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return userPlaylistService.updatePlaylist(playlistId, name, description, isPrivate, userId, coverImage);
    }
    
    /**
     * 删除歌单
     */
    @DeleteMapping("/{id}")
    public Result deletePlaylist(
            @PathVariable("id") Integer playlistId,
            @RequestHeader(value = "token", required = false) String token) {
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return userPlaylistService.deletePlaylist(playlistId, userId);
    }
} 
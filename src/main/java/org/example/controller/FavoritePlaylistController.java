package org.example.controller;

import org.example.service.FavoritePlaylistService;
import org.example.utils.JwtHelper;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite-playlist")
public class FavoritePlaylistController {
    
    @Autowired
    private FavoritePlaylistService favoritePlaylistService;
    
    @Autowired
    private JwtHelper jwtHelper;
    
    /**
     * 收藏歌单
     */
    @PostMapping("/add")
    public Result addFavoritePlaylist(@RequestParam Integer playlistId, @RequestHeader(value = "token", required = false) String token) {
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return favoritePlaylistService.addFavoritePlaylist(playlistId, userId);
    }
    
    /**
     * 取消收藏歌单
     */
    @DeleteMapping("/remove")
    public Result removeFavoritePlaylist(@RequestParam Integer playlistId, @RequestHeader(value = "token", required = false) String token) {
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return favoritePlaylistService.removeFavoritePlaylist(playlistId, userId);
    }
    
    /**
     * 获取用户收藏的所有歌单
     */
    @GetMapping("/list")
    public Result getFavoritePlaylists(@RequestHeader(value = "token", required = false) String token) {
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return favoritePlaylistService.getFavoritePlaylists(userId);
    }
    
    /**
     * 检查用户是否收藏了指定歌单
     */
    @GetMapping("/check")
    public Result checkFavorite(@RequestParam Integer playlistId, @RequestHeader(value = "token", required = false) String token) {
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return favoritePlaylistService.checkFavorite(playlistId, userId);
    }
} 
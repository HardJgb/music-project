package org.example.controller;

import org.example.service.UserSongPlayCountService;
import org.example.utils.JwtHelper;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-songs")
public class UserSongPlayCountController {
    
    @Autowired
    private UserSongPlayCountService userSongPlayCountService;
    
    @Autowired
    private JwtHelper jwtHelper;
    
    /**
     * 增加歌曲播放次数
     */
    @PostMapping("/play")
    public Result recordSongPlay(
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
        
        return userSongPlayCountService.recordSongPlay(userId, songId);
    }
    
    /**
     * 获取用户最常播放的5首歌曲
     */
    @GetMapping("/most-played")
    public Result getMostPlayedSongs(
            @RequestHeader(value = "token", required = false) String token) {
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return userSongPlayCountService.getMostPlayedSongs(userId);
    }
} 
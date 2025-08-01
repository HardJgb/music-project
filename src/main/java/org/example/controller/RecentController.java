package org.example.controller;

import org.example.service.RecentService;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.example.utils.JwtHelper;

@RestController
@RequestMapping("/api/recent")
public class RecentController {
    
    @Autowired
    private RecentService recentService;
    
    @Autowired
    private JwtHelper jwtHelper;
    
    // 添加最近播放记录
    @PostMapping("/add")
    public Result addRecentPlay(@RequestParam Integer songId, @RequestHeader(value = "token", required = false) String token) {
        System.out.println("添加最近播放 - 收到的token: " + token);
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
            System.out.println("解析出的userId: " + userId);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return recentService.addRecentPlay(songId, userId);
    }
    
    // 获取用户最近播放列表
    @GetMapping("/list")
    public Result getRecentPlays(@RequestHeader(value = "token", required = false) String token) {
        System.out.println("获取最近播放列表 - 收到的token: " + token);
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
            System.out.println("解析出的userId: " + userId);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return recentService.getRecentPlays(userId);
    }
    
    // 删除单条最近播放记录
    @DeleteMapping("/delete")
    public Result<?> deleteRecentPlay(@RequestParam Integer songId, @RequestHeader(value = "token", required = false) String token) {
        // Get user ID from token
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("Not logged in");
        }
        
        return recentService.deleteRecentPlay(songId, userId);
    }

    @DeleteMapping("/clear")
    public Result<?> clearRecentPlays(@RequestHeader(value = "token", required = false) String token) {
        // Get user ID from token
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        if (userId == null) {
            return Result.error("Not logged in");
        }
        
        return recentService.clearRecentPlays(userId);
    }
} 
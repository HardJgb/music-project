package org.example.controller;

import org.example.pojo.Favorite;
import org.example.service.FavoriteService;
import org.example.utils.JwtHelper;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private JwtHelper jwtHelper;
    
    // 添加收藏
    @PostMapping
    public Result addFavorite(@RequestBody Favorite favorite, @RequestHeader(value = "token", required = false) String token) {
        System.out.println("收到的token: " + token);  // 调试日志
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
            System.out.println("解析出的userId: " + userId);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return favoriteService.addFavorite(favorite, userId);
    }
    
    // 取消收藏
    @DeleteMapping("/{songId}")
    public Result removeFavorite(@PathVariable Integer songId, @RequestHeader(value = "token", required = false) String token) {
        System.out.println("取消收藏 - 收到的token: " + token);  // 调试日志
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
            System.out.println("解析出的userId: " + userId);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return favoriteService.removeFavorite(songId, userId);
    }
    
    // 获取用户收藏列表
    @GetMapping
    public Result getUserFavorites(@RequestHeader(value = "token", required = false) String token) {
        System.out.println("获取收藏列表 - 收到的token: " + token);  // 调试日志
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
            System.out.println("解析出的userId: " + userId);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return favoriteService.getUserFavorites(userId);
    }
    
    // 检查是否已收藏
    @GetMapping("/check/{songId}")
    public Result checkFavorite(@PathVariable Integer songId, @RequestHeader(value = "token", required = false) String token) {
        System.out.println("检查收藏 - 收到的token: " + token);  // 调试日志
        
        // 从token获取用户ID
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
            System.out.println("解析出的userId: " + userId);
        }
        
        if (userId == null) {
            return Result.error("未登录");
        }
        
        return favoriteService.checkFavorite(songId, userId);
    }
    
    // 添加额外的路径匹配，以支持前端API调用
    @GetMapping("/user/favorite/check/{songId}")
    public Result checkUserFavorite(@PathVariable Integer songId, @RequestHeader(value = "token", required = false) String token) {
        System.out.println("另一路径检查收藏 - 收到的token: " + token);
        return checkFavorite(songId, token);
    }
} 
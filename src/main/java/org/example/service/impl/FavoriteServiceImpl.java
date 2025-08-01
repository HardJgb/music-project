package org.example.service.impl;

import org.example.mapper.FavoriteMapper;
import org.example.pojo.Favorite;
import org.example.service.FavoriteService;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    
    @Autowired
    private FavoriteMapper favoriteMapper;
    
    @Override
    public Result addFavorite(Favorite favorite, Integer userId) {
        // 检查是否已收藏
        int count = favoriteMapper.checkFavorite(userId, favorite.getSongId());
        if (count > 0) {
            return Result.error("已经收藏过这首歌曲");
        }
        
        // 设置用户ID
        favorite.setUserId(userId);
        
        // 添加收藏
        int rows = favoriteMapper.addFavorite(favorite);
        if (rows > 0) {
            return Result.success(null, "收藏成功");
        } else {
            return Result.error("收藏失败");
        }
    }
    
    @Override
    public Result removeFavorite(Integer songId, Integer userId) {
        int rows = favoriteMapper.removeFavorite(userId, songId);
        if (rows > 0) {
            return Result.success(null, "取消收藏成功");
        } else {
            return Result.error("取消收藏失败");
        }
    }
    
    @Override
    public Result getUserFavorites(Integer userId) {
        List<Favorite> favorites = favoriteMapper.getUserFavorites(userId);
        return Result.success(favorites);
    }
    
    @Override
    public Result checkFavorite(Integer songId, Integer userId) {
        int count = favoriteMapper.checkFavorite(userId, songId);
        return Result.success(count > 0);
    }
} 
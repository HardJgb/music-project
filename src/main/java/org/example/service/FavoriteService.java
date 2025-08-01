package org.example.service;

import org.example.pojo.Favorite;
import org.example.utils.Result;

public interface FavoriteService {
    // 添加收藏
    Result addFavorite(Favorite favorite, Integer userId);
    
    // 取消收藏
    Result removeFavorite(Integer songId, Integer userId);
    
    // 获取用户收藏列表
    Result getUserFavorites(Integer userId);
    
    // 检查是否已收藏
    Result checkFavorite(Integer songId, Integer userId);
} 
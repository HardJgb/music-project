package org.example.service;

import org.example.mapper.FavoritePlaylistMapper;
import org.example.mapper.PlaylistMapper;
import org.example.pojo.FavoritePlaylist;
import org.example.pojo.Playlist;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoritePlaylistService {
    
    @Autowired
    private FavoritePlaylistMapper favoritePlaylistMapper;
    
    @Autowired
    private PlaylistMapper playlistMapper;
    
    /**
     * 收藏歌单
     */
    public Result addFavoritePlaylist(Integer playlistId, Integer userId) {
        try {
            // 检查歌单是否存在
            Playlist playlist = playlistMapper.findById(playlistId);
            if (playlist == null) {
                return Result.error("歌单不存在");
            }
            
            // 检查是否已经收藏
            FavoritePlaylist existingRecord = favoritePlaylistMapper.findByUserIdAndPlaylistId(userId, playlistId);
            if (existingRecord != null) {
                return Result.error("已经收藏过这个歌单");
            }
            
            // 创建新收藏记录
            FavoritePlaylist favorite = new FavoritePlaylist();
            favorite.setUserId(userId);
            favorite.setPlaylistId(playlistId);
            favorite.prePersist();
            favoritePlaylistMapper.insert(favorite);
            
            return Result.success("收藏成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("收藏歌单失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消收藏歌单
     */
    public Result removeFavoritePlaylist(Integer playlistId, Integer userId) {
        try {
            // 检查是否已经收藏
            FavoritePlaylist existingRecord = favoritePlaylistMapper.findByUserIdAndPlaylistId(userId, playlistId);
            if (existingRecord == null) {
                return Result.error("未收藏此歌单");
            }
            
            // 删除收藏记录
            int rows = favoritePlaylistMapper.deleteByUserIdAndPlaylistId(userId, playlistId);
            if (rows > 0) {
                return Result.success("取消收藏成功");
            } else {
                return Result.error("取消收藏失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("取消收藏歌单失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户收藏的所有歌单
     */
    public Result getFavoritePlaylists(Integer userId) {
        try {
            // 获取收藏的歌单ID列表
            List<Integer> playlistIds = favoritePlaylistMapper.findPlaylistIdsByUserId(userId);
            List<Playlist> playlists = new ArrayList<>();
            
            // 获取每个歌单的详细信息
            for (Integer playlistId : playlistIds) {
                Playlist playlist = playlistMapper.findById(playlistId);
                if (playlist != null) {
                    playlists.add(playlist);
                }
            }
            
            return Result.success(playlists);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取收藏歌单失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户是否收藏了指定歌单
     */
    public Result checkFavorite(Integer playlistId, Integer userId) {
        try {
            FavoritePlaylist favorite = favoritePlaylistMapper.findByUserIdAndPlaylistId(userId, playlistId);
            boolean isFavorite = (favorite != null);
            return Result.success(isFavorite);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("检查收藏状态失败: " + e.getMessage());
        }
    }
} 
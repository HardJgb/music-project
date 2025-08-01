package org.example.service;

import org.example.mapper.RecentMapper;
import org.example.mapper.SongMapper;
import org.example.pojo.Recent;
import org.example.pojo.Song;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecentService {
    
    @Autowired
    private RecentMapper recentMapper;
    
    @Autowired
    private SongMapper songMapper;
    
    // 添加最近播放记录
    public Result addRecentPlay(Integer songId, Integer userId) {
        try {
            // 检查歌曲是否存在
            Song song = songMapper.selectById(songId);
            if (song == null) {
                return Result.error("歌曲不存在");
            }
            
            // 检查是否已经有这条记录
            Recent existingRecord = recentMapper.findByUserIdAndSongId(userId, songId);
            
            if (existingRecord != null) {
                // 如果记录已存在，更新时间戳
                recentMapper.updateTimestamp(userId, songId);
            } else {
                // 如果记录不存在，创建新记录
                Recent recent = new Recent();
                recent.setUserId(userId);
                recent.setSongId(songId);
                recent.prePersist();
                recentMapper.insert(recent);
            }
            
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加最近播放记录失败");
        }
    }
    
    // 获取用户最近播放列表
    public Result getRecentPlays(Integer userId) {
        try {
            List<Recent> recentPlays = recentMapper.findByUserId(userId);
            List<Map<String, Object>> resultList = new ArrayList<>();
            
            for (Recent recent : recentPlays) {
                Song song = songMapper.selectById(recent.getSongId());
                if (song != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", recent.getId());
                    item.put("songId", recent.getSongId());
                    item.put("playTime", recent.getPlayTime());
                    item.put("song", song);
                    resultList.add(item);
                }
            }
            
            return Result.success(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取最近播放列表失败");
        }
    }
    
    // 删除单条最近播放记录
    public Result deleteRecentPlay(Integer songId, Integer userId) {
        try {
            Recent record = recentMapper.findByUserIdAndSongId(userId, songId);
            if (record == null) {
                return Result.error("记录不存在");
            }
            
            int rows = recentMapper.deleteByUserIdAndSongId(userId, songId);
            if (rows > 0) {
                return Result.success(null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除最近播放记录失败");
        }
    }
    
    // 清空用户的所有最近播放记录
    public Result clearRecentPlays(Integer userId) {
        try {
            int rows = recentMapper.deleteAllByUserId(userId);
            if (rows >= 0) {
                return Result.success(null);
            } else {
                return Result.error("清空失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("清空最近播放记录失败");
        }
    }
} 
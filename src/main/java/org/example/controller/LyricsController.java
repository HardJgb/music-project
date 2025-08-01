package org.example.controller;

import org.example.pojo.SongLyrics;
import org.example.service.LyricsService;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 歌词接口控制器
 * 处理歌词的查询、添加、修改操作
 */
@RestController
public class LyricsController {

    @Autowired
    private LyricsService lyricsService;

    /**
     * 获取指定歌曲的歌词
     * @param songId 歌曲ID
     * @return 歌词信息
     */
    @GetMapping("/api/songs/{songId}/lyrics")
    public Result<SongLyrics> getLyricsBySongId(@PathVariable Integer songId) {
        try {
            SongLyrics lyrics = lyricsService.getLyricsBySongId(songId);
            if (lyrics != null) {
                return Result.success(lyrics);
            } else {
                return Result.error("未找到该歌曲的歌词");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取歌词失败: " + e.getMessage());
        }
    }
    
    /**
     * 简单接口适配前端
     * @param songId 歌曲ID
     * @return 歌词信息
     */
    @GetMapping("/api/songs/lyrics/{songId}")
    public Result<SongLyrics> getLyricsBySongIdSimple(@PathVariable Integer songId) {
        return getLyricsBySongId(songId);
    }
    
    /**
     * 直接匹配前端调用的路径
     * @param songId 歌曲ID
     * @return 歌词信息
     */
    @GetMapping("/api/lyrics/{songId}")
    public Result<SongLyrics> getLyricsByDirectPath(@PathVariable Integer songId) {
        return getLyricsBySongId(songId);
    }

    /**
     * 添加或更新歌曲歌词
     * @param songId 歌曲ID
     * @param lyrics 歌词信息
     * @return 操作结果
     */
    @PostMapping("/api/songs/{songId}/lyrics")
    public Result<SongLyrics> saveLyrics(@PathVariable Integer songId, @RequestBody SongLyrics lyrics) {
        try {
            // 设置歌曲ID
            lyrics.setSongId(songId);
            
            // 检查歌词是否已存在
            SongLyrics existingLyrics = lyricsService.getLyricsBySongId(songId);
            
            int result;
            if (existingLyrics != null) {
                // 更新现有歌词
                result = lyricsService.updateLyrics(lyrics);
            } else {
                // 添加新歌词
                result = lyricsService.addLyrics(lyrics);
            }
            
            if (result > 0) {
                return Result.success(lyrics);
            } else {
                return Result.error("保存歌词失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存歌词失败: " + e.getMessage());
        }
    }

    /**
     * 删除歌词
     * @param songId 歌曲ID
     * @return 操作结果
     */
    @DeleteMapping("/api/songs/{songId}/lyrics")
    public Result<String> deleteLyrics(@PathVariable Integer songId) {
        try {
            int result = lyricsService.deleteLyrics(songId);
            if (result > 0) {
                return Result.success("删除歌词成功");
            } else {
                return Result.error("未找到该歌曲的歌词");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除歌词失败: " + e.getMessage());
        }
    }
} 
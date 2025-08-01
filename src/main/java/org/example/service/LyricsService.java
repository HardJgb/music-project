package org.example.service;

import org.example.mapper.LyricsMapper;
import org.example.pojo.SongLyrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 歌词服务类，负责歌词相关的业务逻辑
 */
@Service
public class LyricsService {
    
    @Autowired
    private LyricsMapper lyricsMapper;
    
    /**
     * 根据歌曲ID获取歌词
     * @param songId 歌曲ID
     * @return 歌词对象
     */
    public SongLyrics getLyricsBySongId(Integer songId) {
        return lyricsMapper.findBySongId(songId);
    }
    
    /**
     * 添加歌词
     * @param lyrics 歌词信息
     * @return 影响的行数
     */
    public int addLyrics(SongLyrics lyrics) {
        // 设置默认格式，如果没有指定
        if (lyrics.getFormat() == null || lyrics.getFormat().isEmpty()) {
            lyrics.setFormat("LRC");
        }
        return lyricsMapper.insert(lyrics);
    }
    
    /**
     * 更新歌词
     * @param lyrics 歌词信息
     * @return 影响的行数
     */
    public int updateLyrics(SongLyrics lyrics) {
        return lyricsMapper.update(lyrics);
    }
    
    /**
     * 删除歌词
     * @param songId 歌曲ID
     * @return 影响的行数
     */
    public int deleteLyrics(Integer songId) {
        return lyricsMapper.deleteBySongId(songId);
    }
} 
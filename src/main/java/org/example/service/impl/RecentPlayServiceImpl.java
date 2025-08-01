package org.example.service.impl;

import org.example.mapper.RecentPlayMapper;
import org.example.pojo.RecentPlay;
import org.example.service.RecentPlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecentPlayServiceImpl implements RecentPlayService {

    @Autowired
    private RecentPlayMapper recentPlayMapper;

    @Override
    @Transactional
    public void addRecentPlay(Integer userId, Integer songId) {
        // 先删除相同的记录
        recentPlayMapper.deleteByUserIdAndSongId(userId, songId);
        
        // 添加新记录
        RecentPlay recentPlay = new RecentPlay();
        recentPlay.setUserId(userId);
        recentPlay.setSongId(songId);
        recentPlay.setPlayTime(LocalDateTime.now());  // 设置当前时间为播放时间
        recentPlayMapper.insert(recentPlay);
    }

    @Override
    public List<RecentPlay> getRecentPlays(Integer userId, Integer limit) {
        return recentPlayMapper.selectByUserId(userId, limit);
    }

    @Override
    @Transactional
    public void deleteRecentPlay(Integer userId, Integer songId) {
        recentPlayMapper.deleteByUserIdAndSongId(userId, songId);
    }

    @Override
    @Transactional
    public void clearRecentPlays(Integer userId) {
        recentPlayMapper.deleteByUserId(userId);
    }
} 
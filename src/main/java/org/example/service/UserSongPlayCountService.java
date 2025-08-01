package org.example.service;

import org.example.mapper.SongMapper;
import org.example.mapper.UserSongPlayCountMapper;
import org.example.pojo.Song;
import org.example.pojo.UserSongPlayCount;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserSongPlayCountService {
    
    @Autowired
    private UserSongPlayCountMapper userSongPlayCountMapper;
    
    @Autowired
    private SongMapper songMapper;
    
    /**
     * 记录用户播放歌曲，增加播放次数
     * @param userId 用户ID
     * @param songId 歌曲ID
     * @return 操作结果
     */
    @Transactional
    public Result recordSongPlay(Integer userId, Integer songId) {
        try {
            if (userId == null || songId == null) {
                return Result.error("参数错误");
            }
            
            // 查询是否已有记录
            UserSongPlayCount record = userSongPlayCountMapper.findByUserIdAndSongId(userId, songId);
            
            if (record == null) {
                // 没有记录，创建新记录
                record = new UserSongPlayCount();
                record.setUserId(userId);
                record.setSongId(songId);
                record.setPlayCount(1);
                record.setLastPlayTime(LocalDateTime.now());
                userSongPlayCountMapper.insert(record);
            } else {
                // 已有记录，更新播放次数和时间
                record.setPlayCount(record.getPlayCount() + 1);
                record.setLastPlayTime(LocalDateTime.now());
                userSongPlayCountMapper.updateById(record);
            }
            
            return Result.success("记录成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("记录播放次数失败");
        }
    }
    
    /**
     * 获取用户最常播放的5首歌曲
     * @param userId 用户ID
     * @return 歌曲列表
     */
    public Result getMostPlayedSongs(Integer userId) {
        try {
            if (userId == null) {
                return Result.error("参数错误");
            }
            
            // 查询用户播放最多的5首歌曲
            List<UserSongPlayCount> playRecords = userSongPlayCountMapper.findMostPlayedByUserId(userId, 5);
            
            // 没有记录返回空列表
            if (playRecords == null || playRecords.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            
            // 获取完整的歌曲信息
            List<Song> songs = new ArrayList<>();
            for (UserSongPlayCount record : playRecords) {
                Song song = songMapper.selectById(record.getSongId());
                if (song != null) {
                    // 添加播放次数到歌曲属性
                    song.setUserPlayCount(record.getPlayCount());
                    songs.add(song);
                }
            }
            
            return Result.success(songs);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取最常播放歌曲失败");
        }
    }
} 
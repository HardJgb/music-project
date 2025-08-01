package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.mapper.SongMapper;
import org.example.pojo.Song;
import org.example.service.SongService;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class SongServiceImpl implements SongService {
    
    @Autowired
    private SongMapper songMapper;
    
    @Override
    public List<Song> getAllSongs() {
        return songMapper.findAll();
    }
    
    @Override
    public Song getSongById(Integer id) {
        return songMapper.findById(id);
    }
    
    @Override
    public List<Song> getSongsByPlaylistId(Integer playlistId) {
        return songMapper.findByPlaylistId(playlistId);
    }
    
    @Override
    public int createSong(Song song) {
        if (song.getPlayCount() == null) {
            song.setPlayCount(0); // 设置默认播放次数为0
        }
        return songMapper.insertSong(song);
    }
    
    @Override
    public int updateSong(Song song) {
        return songMapper.updateSong(song);
    }
    
    @Override
    @Transactional
    public int deleteSong(Integer id) {
        try {
            // 先删除关联表中的数据，确保顺序正确
            
            // 1. 删除最近播放记录中关联的数据
            songMapper.deleteRecentPlayBySongId(id);
            
            // 2. 删除收藏表中关联的数据
            songMapper.deleteFavoriteBySongId(id);
            
            // 3. 删除评论表中关联的数据
            songMapper.deleteCommentsBySongId(id);
            
            // 4. 删除专辑歌曲关联表中的数据
            songMapper.deleteAlbumSongBySongId(id);
            
            // 5. 最后删除歌曲
            return songMapper.deleteSong(id);
        } catch (Exception e) {
            // 记录错误并抛出异常
            System.err.println("删除歌曲时发生错误: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public int incrementPlayCount(Integer id) {
        return songMapper.incrementPlayCount(id);
    }
    
    /**
     * 获取按播放次数排序的歌曲列表
     * @return 按播放次数降序排序的歌曲列表
     */
    @Override
    public List<Song> getSongsByPlayCount() {
        // 直接使用数据库查询按播放次数排序
        return songMapper.findAllOrderByPlayCount();
    }

    /**
     * 获取最新歌曲列表
     * @return 按创建时间降序排序的歌曲列表
     */
    @Override
    public List<Song> getLatestSongs() {
        return songMapper.findAllOrderByCreateTime();
    }

    /**
     * 获取所有歌手列表
     * @return 不重复的歌手名称列表
     */
    @Override
    public List<String> getAllArtists() {
        return songMapper.findAllArtists();
    }

    /**
     * 根据歌手名称获取歌曲
     * @param artist 歌手名称
     * @return 该歌手的所有歌曲
     */
    @Override
    public List<Song> getSongsByArtist(String artist) {
        return songMapper.findByArtist(artist);
    }

    /**
     * 分页查询歌曲
     * @param page 页码
     * @param pageSize 每页数量
     * @param query 查询关键字
     * @return 分页结果
     */
    @Override
    public Result getSongsByPage(int page, int pageSize, String query) {
        try {
            // 使用MyBatisPlus的Page进行分页
            Page<Song> songPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Song> queryWrapper = new LambdaQueryWrapper<>();
            
            // 如果有查询条件，添加查询条件
            if (query != null && !query.isEmpty()) {
                queryWrapper.like(Song::getName, query)
                        .or()
                        .like(Song::getArtist, query);
            }
            
            // 执行分页查询
            Page<Song> resultPage = songMapper.selectPage(songPage, queryWrapper);
            
            // 获取查询结果
            List<Song> songs = resultPage.getRecords();
            long total = resultPage.getTotal();
            
            // 构建返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("list", songs);
            data.put("total", total);
            data.put("pageSize", pageSize);
            data.put("currentPage", page);
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("查询歌曲列表失败: " + e.getMessage());
        }
    }
} 
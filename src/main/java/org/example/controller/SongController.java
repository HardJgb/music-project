package org.example.controller;


import org.example.pojo.Song;
import org.example.service.SongService;
import org.example.service.UserSongPlayCountService;
import org.example.utils.JwtHelper;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/songs")
public class SongController {
    
    @Autowired
    private SongService songService;
    
    @Autowired
    private UserSongPlayCountService userSongPlayCountService;
    
    @Autowired
    private JwtHelper jwtHelper;
    
    @GetMapping
    public Result<List<Song>> getAllSongs() {
        List<Song> songs = songService.getAllSongs();
        return Result.success(songs);
    }
    
    @GetMapping("/{id}")
    public Result<Song> getSongById(@PathVariable Integer id) {
        Song song = songService.getSongById(id);
        if (song != null) {
            return Result.success(song);
        } else {
            return Result.error("歌曲不存在");
        }
    }
    
    @GetMapping("/playlist/{playlistId}")
    public Result<List<Song>> getSongsByPlaylistId(@PathVariable Integer playlistId) {
        List<Song> songs = songService.getSongsByPlaylistId(playlistId);
        return Result.success(songs);
    }
    
    @PostMapping
    public Result<Song> createSong(@RequestBody Song song) {
        int result = songService.createSong(song);
        if (result > 0) {
            return Result.success(song);
        } else {
            return Result.error("创建歌曲失败");
        }
    }
    
    @PutMapping("/{id}")
    public Result<Song> updateSong(@PathVariable Integer id, @RequestBody Song song) {
        song.setId(id);
        int result = songService.updateSong(song);
        if (result > 0) {
            return Result.success(song);
        } else {
            return Result.error("更新歌曲失败");
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deleteSong(@PathVariable Integer id) {
        int result = songService.deleteSong(id);
        if (result > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除歌曲失败");
        }
    }
    
    @PostMapping("/{id}/play")
    public ResponseEntity<Map<String, Object>> incrementPlayCount(
            @PathVariable Integer id,
            @RequestHeader(value = "token", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 先检查歌曲是否存在
            Song song = songService.getSongById(id);
            if (song == null) {
                response.put("code", 404);
                response.put("message", "歌曲不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            // 增加歌曲全局播放次数
            int result = songService.incrementPlayCount(id);
            
            // 如果用户已登录，记录用户个人播放统计
            if (token != null && !token.isEmpty()) {
                Integer userId = jwtHelper.getUserId(token);
                if (userId != null) {
                    userSongPlayCountService.recordSongPlay(userId, id);
                }
            }
            
            if (result > 0) {
                // 重新获取更新后的歌曲信息
                song = songService.getSongById(id);
                response.put("code", 200);
                response.put("message", "播放次数增加成功");
                response.put("data", song);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "播放次数增加失败");
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/ranking")
    public Result<List<Song>> getSongsByPlayCount() {
        List<Song> songs = songService.getSongsByPlayCount();
        return Result.success(songs);
    }

    @GetMapping("/latest")
    public Result<List<Song>> getLatestSongs() {
        List<Song> songs = songService.getLatestSongs();
        return Result.success(songs);
    }
    
    /**
     * 分页查询歌曲
     */
    @GetMapping("/page")
    public Result getSongsByPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "query", required = false) String query) {
        return songService.getSongsByPage(page, pageSize, query);
    }
    
    @GetMapping("/artists")
    public Result<List<String>> getAllArtists() {
        List<String> artists = songService.getAllArtists();
        return Result.success(artists);
    }
    
    @GetMapping("/artist/{artist}")
    public Result<List<Song>> getSongsByArtist(@PathVariable String artist) {
        List<Song> songs = songService.getSongsByArtist(artist);
        return Result.success(songs);
    }
} 
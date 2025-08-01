package org.example.controller;

import org.example.pojo.Playlist;
import org.example.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {
    
    @Autowired
    private PlaylistService playlistService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPlaylists() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Playlist> playlists = playlistService.getAllPlaylists();
            response.put("code", 200);
            response.put("message", "获取所有歌单成功");
            response.put("data", playlists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取歌单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/recommended")
    public ResponseEntity<Map<String, Object>> getRecommendedPlaylists() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Playlist> playlists = playlistService.getRecommendedPlaylists();
            response.put("code", 200);
            response.put("message", "获取推荐歌单成功");
            response.put("data", playlists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取推荐歌单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlaylistById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Playlist playlist = playlistService.getPlaylistById(id);
            if (playlist != null) {
                response.put("code", 200);
                response.put("message", "获取歌单详情成功");
                response.put("data", playlist);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 404);
                response.put("message", "歌单不存在");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取歌单详情失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPlaylist(@RequestBody Playlist playlist) {
        Map<String, Object> response = new HashMap<>();
        try {
            int result = playlistService.createPlaylist(playlist);
            if (result > 0) {
                response.put("code", 200);
                response.put("message", "创建歌单成功");
                response.put("data", playlist);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "创建歌单失败");
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "创建歌单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePlaylist(@PathVariable Integer id, @RequestBody Playlist playlist) {
        Map<String, Object> response = new HashMap<>();
        try {
            playlist.setId(id);
            int result = playlistService.updatePlaylist(playlist);
            if (result > 0) {
                response.put("code", 200);
                response.put("message", "更新歌单成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "更新歌单失败");
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "更新歌单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePlaylist(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            int result = playlistService.deletePlaylist(id);
            if (result > 0) {
                response.put("code", 200);
                response.put("message", "删除歌单成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "删除歌单失败");
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "删除歌单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> categories = playlistService.getAllCategories();
            response.put("code", 200);
            response.put("message", "获取所有歌单分类成功");
            response.put("data", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取歌单分类失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getPlaylistsByCategory(@PathVariable String category) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Playlist> playlists = playlistService.getPlaylistsByCategory(category);
            response.put("code", 200);
            response.put("message", "获取分类歌单成功");
            response.put("data", playlists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取分类歌单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 添加歌曲到歌单
     */
    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Map<String, Object>> addSongToPlaylist(@PathVariable Integer playlistId, @PathVariable Integer songId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int result = playlistService.addSongToPlaylist(playlistId, songId);
            if (result > 0) {
                response.put("code", 200);
                response.put("message", "歌曲添加成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "歌曲添加失败");
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "添加歌曲失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 从歌单中删除歌曲
     */
    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Map<String, Object>> removeSongFromPlaylist(@PathVariable Integer playlistId, @PathVariable Integer songId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int result = playlistService.removeSongFromPlaylist(playlistId, songId);
            if (result > 0) {
                response.put("code", 200);
                response.put("message", "歌曲删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "歌曲删除失败，可能歌曲不在歌单中");
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "删除歌曲失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 批量添加歌曲到歌单
     */
    @PostMapping("/{playlistId}/songs/batch")
    public ResponseEntity<Map<String, Object>> addSongsToPlaylist(@PathVariable Integer playlistId, @RequestBody List<Integer> songIds) {
        Map<String, Object> response = new HashMap<>();
        try {
            int successCount = playlistService.addSongsToPlaylist(playlistId, songIds);
            response.put("code", 200);
            response.put("message", "成功添加 " + successCount + " 首歌曲");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "批量添加歌曲失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 清空歌单中的所有歌曲
     */
    @DeleteMapping("/{playlistId}/songs")
    public ResponseEntity<Map<String, Object>> clearPlaylistSongs(@PathVariable Integer playlistId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int result = playlistService.clearPlaylistSongs(playlistId);
            response.put("code", 200);
            response.put("message", "已清空歌单中的所有歌曲");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "清空歌单歌曲失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
} 
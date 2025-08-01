package org.example.controller;

import org.example.pojo.Album;
import org.example.pojo.Song;
import org.example.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @GetMapping
    public ResponseEntity<List<Album>> getAllAlbums() {
        List<Album> albums = albumService.getAllAlbums();
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Album>> getLatestAlbums() {
        List<Album> albums = albumService.getLatestAlbums();
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAlbumById(@PathVariable Integer id) {
        Album album = albumService.getAlbumById(id);
        if (album == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "专辑不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(album);
    }

    @GetMapping("/artist/{artist}")
    public ResponseEntity<List<Album>> getAlbumsByArtist(@PathVariable String artist) {
        List<Album> albums = albumService.getAlbumsByArtist(artist);
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Album>> searchAlbumsByName(@RequestParam String name) {
        List<Album> albums = albumService.searchAlbumsByName(name);
        return ResponseEntity.ok(albums);
    }

    @PostMapping
    public ResponseEntity<?> addAlbum(@RequestBody Album album) {
        int result = albumService.addAlbum(album);
        if (result > 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "专辑添加成功");
            response.put("id", album.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "专辑添加失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAlbum(@PathVariable Integer id, @RequestBody Album album) {
        album.setId(id);
        int result = albumService.updateAlbum(album);
        if (result > 0) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "专辑更新成功");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "专辑更新失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlbum(@PathVariable Integer id) {
        int result = albumService.deleteAlbum(id);
        if (result > 0) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "专辑删除成功");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "专辑删除失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}/songs")
    public ResponseEntity<List<Song>> getAlbumSongs(@PathVariable Integer id) {
        List<Song> songs = albumService.getAlbumSongs(id);
        return ResponseEntity.ok(songs);
    }

    @PostMapping("/{albumId}/songs/{songId}")
    public ResponseEntity<?> addSongToAlbum(
            @PathVariable Integer albumId,
            @PathVariable Integer songId,
            @RequestParam(required = false, defaultValue = "0") Integer trackNumber) {
        
        int result = albumService.addSongToAlbum(albumId, songId, trackNumber);
        if (result > 0) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "歌曲添加到专辑成功");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "歌曲添加到专辑失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{albumId}/songs/{songId}")
    public ResponseEntity<?> removeSongFromAlbum(
            @PathVariable Integer albumId,
            @PathVariable Integer songId) {
        
        int result = albumService.removeSongFromAlbum(albumId, songId);
        if (result > 0) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "歌曲从专辑移除成功");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "歌曲从专辑移除失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/cover")
    public ResponseEntity<?> uploadAlbumCover(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "文件为空");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            String fileName = albumService.uploadAlbumCover(id, file.getBytes(), file.getOriginalFilename());
            Map<String, String> response = new HashMap<>();
            response.put("message", "专辑封面上传成功");
            response.put("fileName", fileName);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "专辑封面上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importExistingAlbums() {
        int count = albumService.importExistingAlbums();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "成功导入专辑");
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/page")
    public ResponseEntity<?> getAlbumsList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "query", required = false) String query) {

        // Get the albums list based on query if provided
        List<Album> allAlbums;
        if (query != null && !query.trim().isEmpty()) {
            allAlbums = albumService.searchAlbumsByName(query);
        } else {
            allAlbums = albumService.getAllAlbums();
        }

        // Calculate pagination
        int total = allAlbums.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        // Get the sublist for the current page
        List<Album> pagedAlbums = startIndex < total ? 
                allAlbums.subList(startIndex, endIndex) : 
                List.of();

        // Create response structure matching frontend expectations
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("list", pagedAlbums);
        data.put("total", total);
        
        response.put("code", 200);
        response.put("message", "Success");
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{albumId}/songs/{songId}")
    public ResponseEntity<?> updateSongTrackNumber(
            @PathVariable Integer albumId,
            @PathVariable Integer songId,
            @RequestBody Map<String, Integer> requestBody) {
        
        Integer trackNumber = requestBody.get("trackNumber");
        if (trackNumber == null || trackNumber < 1) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "无效的曲目编号");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        int result = albumService.updateTrackNumber(albumId, songId, trackNumber);
        if (result > 0) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "曲目编号更新成功");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "曲目编号更新失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 
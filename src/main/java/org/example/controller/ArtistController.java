package org.example.controller;

import org.example.pojo.Artist;
import org.example.service.ArtistService;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    @Autowired
    private ArtistService artistService;

    /**
     * 获取所有歌手
     */
    @GetMapping
    public Result<List<Artist>> getAllArtists() {
        try {
            List<Artist> artists = artistService.getAllArtists();
            return Result.success(artists);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取歌手列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取歌手详情
     */
    @GetMapping("/{id}")
    public Result<Artist> getArtistById(@PathVariable Integer id) {
        try {
            Artist artist = artistService.getArtistById(id);
            if (artist == null) {
                return Result.error("歌手不存在");
            }
            return Result.success(artist);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取歌手详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据名称获取歌手
     */
    @GetMapping("/name/{name}")
    public Result<Artist> getArtistByName(@PathVariable String name) {
        try {
            Artist artist = artistService.getArtistByName(name);
            if (artist == null) {
                return Result.error("歌手不存在");
            }
            return Result.success(artist);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取歌手详情失败: " + e.getMessage());
        }
    }

    /**
     * 添加歌手
     */
    @PostMapping
    public Result<Artist> addArtist(@RequestBody Artist artist) {
        try {
            int result = artistService.addArtist(artist);
            if (result > 0) {
                return Result.success(artist);
            } else {
                return Result.error("添加歌手失败");
            }
        } catch (Exception e) {
            return Result.error("添加歌手失败: " + e.getMessage());
        }
    }

    /**
     * 更新歌手信息
     */
    @PutMapping("/{id}")
    public Result<Artist> updateArtist(@PathVariable Integer id, @RequestBody Artist artist) {
        try {
            artist.setId(id);
            int result = artistService.updateArtist(artist);
            if (result > 0) {
                return Result.success(artist);
            } else {
                return Result.error("更新歌手信息失败");
            }
        } catch (Exception e) {
            return Result.error("更新歌手信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除歌手
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteArtist(@PathVariable Integer id) {
        try {
            int result = artistService.deleteArtist(id);
            if (result > 0) {
                return Result.success(null);
            } else {
                return Result.error("删除歌手失败");
            }
        } catch (Exception e) {
            return Result.error("删除歌手失败: " + e.getMessage());
        }
    }

    /**
     * 上传歌手头像
     */
    @PostMapping("/{id}/avatar")
    public Result<String> uploadAvatar(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("上传的文件为空");
            }
            
            String avatarUrl = artistService.uploadArtistAvatar(id, file.getBytes(), file.getOriginalFilename());
            return Result.success(avatarUrl);
        } catch (IOException e) {
            return Result.error("文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("上传头像失败: " + e.getMessage());
        }
    }

    /**
     * 导入现有歌手数据
     */
    @PostMapping("/import")
    public Result<Integer> importExistingArtists() {
        try {
            int count = artistService.importExistingArtists();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("导入歌手数据失败: " + e.getMessage());
        }
    }
} 
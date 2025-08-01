package org.example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("album_song")
public class AlbumSong {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer albumId;
    private Integer songId;
    private Integer trackNumber; // 歌曲在专辑中的曲目编号

    // 构造函数
    public AlbumSong() {
    }

    public AlbumSong(Integer albumId, Integer songId, Integer trackNumber) {
        this.albumId = albumId;
        this.songId = songId;
        this.trackNumber = trackNumber;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Integer albumId) {
        this.albumId = albumId;
    }

    public Integer getSongId() {
        return songId;
    }

    public void setSongId(Integer songId) {
        this.songId = songId;
    }

    public Integer getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(Integer trackNumber) {
        this.trackNumber = trackNumber;
    }
} 
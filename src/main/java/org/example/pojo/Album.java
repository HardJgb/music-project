package org.example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;
import java.util.List;

@TableName("album")
public class Album {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String artist;
    private String description;
    private String coverImg;
    private Date releaseDate;
    private Date createTime;
    
    @TableField(exist = false)
    private Integer songCount; // 专辑包含的歌曲数量，不映射到数据库
    
    @TableField(exist = false)
    private List<Song> songs; // 专辑包含的歌曲列表，不映射到数据库

    // 构造函数
    public Album() {
    }

    public Album(Integer id, String name, String artist, String description, String coverImg, Date releaseDate) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.description = description;
        this.coverImg = coverImg;
        this.releaseDate = releaseDate;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getSongCount() {
        return songCount;
    }

    public void setSongCount(Integer songCount) {
        this.songCount = songCount;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
} 
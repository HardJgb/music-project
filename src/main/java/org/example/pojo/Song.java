package org.example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("song")
public class Song {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String artist;
    private Integer duration;
    private String fileUrl;
    private String coverImg;
    private Date createTime;
    private String lyricUrl;
    private Integer playCount;
    
    @TableField(exist = false)
    private Integer userPlayCount; // 用户个人播放次数，不映射到数据库
    
    @TableField(exist = false)
    private Integer trackNumber; // 曲目编号，仅在查询专辑歌曲时使用，不映射到数据库

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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getLyricUrl() {
        return lyricUrl;
    }

    public void setLyricUrl(String lyricUrl) {
        this.lyricUrl = lyricUrl;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public Integer getUserPlayCount() {
        return userPlayCount;
    }
    
    public void setUserPlayCount(Integer userPlayCount) {
        this.userPlayCount = userPlayCount;
    }
    
    public Integer getTrackNumber() {
        return trackNumber;
    }
    
    public void setTrackNumber(Integer trackNumber) {
        this.trackNumber = trackNumber;
    }
} 
package org.example.pojo;

import java.util.Date;

/**
 * 歌词实体类，对应songs_lyrics表
 */
public class SongLyrics {
    
    private Integer id;
    private Integer songId;
    private String content;
    private String format;
    private String source;
    private Date createdAt;
    private Date updatedAt;
    
    // 无参构造函数
    public SongLyrics() {
    }
    
    // 带参构造函数
    public SongLyrics(Integer songId, String content, String format, String source) {
        this.songId = songId;
        this.content = content;
        this.format = format;
        this.source = source;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getSongId() {
        return songId;
    }
    
    public void setSongId(Integer songId) {
        this.songId = songId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "SongLyrics{" +
                "id=" + id +
                ", songId=" + songId +
                ", content='" + (content != null ? content.substring(0, Math.min(20, content.length())) + "..." : null) + '\'' +
                ", format='" + format + '\'' +
                ", source='" + source + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 
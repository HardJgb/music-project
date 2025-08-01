package org.example.pojo;

public class Artist {
    private Integer id;
    private String name;
    private String avatarUrl;
    private String description;  // 歌手描述，可选

    // 构造函数
    public Artist() {
    }

    public Artist(Integer id, String name, String avatarUrl, String description) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.description = description;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
} 
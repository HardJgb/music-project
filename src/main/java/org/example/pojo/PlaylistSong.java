package org.example.pojo;

/**
 * 歌单歌曲关联实体类
 */
public class PlaylistSong {
    private Integer id;
    private Integer playlistId;
    private Integer songId;
    
    public PlaylistSong() {
    }
    
    public PlaylistSong(Integer playlistId, Integer songId) {
        this.playlistId = playlistId;
        this.songId = songId;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getPlaylistId() {
        return playlistId;
    }
    
    public void setPlaylistId(Integer playlistId) {
        this.playlistId = playlistId;
    }
    
    public Integer getSongId() {
        return songId;
    }
    
    public void setSongId(Integer songId) {
        this.songId = songId;
    }
    
    @Override
    public String toString() {
        return "PlaylistSong{" +
                "id=" + id +
                ", playlistId=" + playlistId +
                ", songId=" + songId +
                '}';
    }
} 
package org.example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_playlist_song")
public class UserPlaylistSong {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer userPlaylistId;
    
    private Integer songId;
    
    private LocalDateTime createTime;
} 
package org.example.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RecentPlay {
    private Integer id;
    private Integer userId;
    private Integer songId;
    private LocalDateTime playTime;
    
    // 关联的歌曲信息
    private Song song;
} 
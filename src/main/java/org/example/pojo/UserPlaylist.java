package org.example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_playlist")
public class UserPlaylist {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer userId;
    
    private String name;
    
    private String coverImg;
    
    private String description;
    
    private Boolean isPrivate;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
} 
package org.example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("recent_play")
public class Recent {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer userId;
    
    private Integer songId;
    
    private LocalDateTime playTime;
    
    // 在插入时自动设置时间
    public void prePersist() {
        if (this.playTime == null) {
            this.playTime = LocalDateTime.now();
        }
    }
} 
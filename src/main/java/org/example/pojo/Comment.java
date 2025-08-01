package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment {
    private Integer id;
    private Integer userId;
    private Integer songId;
    private String content;
    private Integer parentId;
    private Integer replyToId;
    private Integer likeCount;
    private Date createTime;
    private Date updateTime;
    
    // 扩展字段，不在数据库中
    private String username;
    private String userAvatar;
    private String replyToUsername;
    private Boolean isAuthor;
    private Boolean liked;
    private List<Comment> replies;
    private Integer totalReplies;
} 
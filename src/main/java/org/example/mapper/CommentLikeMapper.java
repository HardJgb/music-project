package org.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.pojo.CommentLike;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentLikeMapper {
    
    // 添加点赞记录
    int insert(CommentLike commentLike);
    
    // 删除点赞记录
    int deleteByUserIdAndCommentId(@Param("userId") Integer userId, @Param("commentId") Integer commentId);
    
    // 查询用户是否点赞过该评论
    CommentLike findByUserIdAndCommentId(@Param("userId") Integer userId, @Param("commentId") Integer commentId);
    
    // 查询用户点赞的所有评论ID
    List<Integer> findCommentIdsByUserId(@Param("userId") Integer userId, @Param("commentIds") List<Integer> commentIds);
    
    // 删除评论的所有点赞记录
    int deleteByCommentId(@Param("commentId") Integer commentId);
} 
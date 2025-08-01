package org.example.service;

import org.example.pojo.Comment;
import org.example.utils.Result;

import java.util.List;
import java.util.Map;

public interface CommentService {
    
    // 获取歌曲评论列表
    Result getCommentsBySongId(Integer songId, Integer page, Integer pageSize, String sortBy, Integer userId);
    
    // 获取评论回复列表
    Result getRepliesByCommentId(Integer commentId, Integer page, Integer pageSize, Integer userId);
    
    // 发表评论
    Result addComment(Integer userId, Integer songId, String content);
    
    // 回复评论
    Result replyComment(Integer userId, Integer commentId, Integer replyToId, String content);
    
    // 点赞评论
    Result likeComment(Integer userId, Integer commentId);
    
    // 取消点赞
    Result unlikeComment(Integer userId, Integer commentId);
    
    // 获取用户是否点赞了指定评论
    List<Integer> getUserLikedCommentIds(Integer userId, List<Integer> commentIds);
    
    // 直接获取歌曲评论列表（无需用户关联）
    List<Comment> getDirectCommentsBySongId(Integer songId, Integer page, Integer pageSize, String sortBy);
    
    // 获取歌曲评论总数
    int countCommentsBySongId(Integer songId);
    
    // 删除评论
    Result deleteComment(Integer userId, Integer commentId, boolean isAdmin);
} 
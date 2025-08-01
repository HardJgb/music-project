package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pojo.Comment;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface CommentMapper {
    
    // 根据歌曲ID获取评论列表
    List<Comment> findBySongId(@Param("songId") Integer songId, 
                            @Param("offset") int offset, 
                            @Param("limit") Integer limit,
                            @Param("sortByLikes") boolean sortByLikes);
    
    // 根据歌曲ID直接获取评论列表（不关联用户表）
    List<Comment> findBySongIdDirect(@Param("songId") Integer songId, 
                                  @Param("offset") int offset, 
                                  @Param("limit") Integer limit,
                                  @Param("sortByLikes") boolean sortByLikes);
    
    // 获取评论总数
    int countBySongId(@Param("songId") Integer songId);
    
    // 获取评论的回复列表
    List<Comment> findRepliesByParentId(@Param("parentId") Integer parentId,
                                     @Param("offset") int offset,
                                     @Param("limit") Integer limit);
    
    // 获取回复总数
    int countRepliesByParentId(@Param("parentId") Integer parentId);
    
    // 添加评论
    int insert(Comment comment);
    
    // 根据ID获取评论
    Comment findById(@Param("id") Integer id);
    
    // 更新评论点赞数
    int updateLikeCount(@Param("id") Integer id, @Param("increment") int increment);
    
    // 获取评论详情（包含用户信息）
    Comment findCommentWithUserInfo(@Param("id") Integer id);
    
    // 获取用户详细信息，用于拼接到评论对象中
    Comment findUserInfoById(@Param("id") Integer id);
    
    // 根据评论ID删除评论
    int deleteById(@Param("id") Integer id);
    
    // 获取某个评论的所有回复
    List<Comment> findRepliesByCommentId(@Param("commentId") Integer commentId,
                                      @Param("offset") Integer offset,
                                      @Param("pageSize") Integer pageSize);
    
    // 减少父评论的回复计数
    int decrementReplyCount(@Param("parentId") Integer parentId);
} 
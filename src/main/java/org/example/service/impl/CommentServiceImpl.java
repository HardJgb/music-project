package org.example.service.impl;

import org.example.mapper.CommentLikeMapper;
import org.example.mapper.CommentMapper;
import org.example.mapper.SongMapper;
import org.example.mapper.UserMapper;
import org.example.pojo.Comment;
import org.example.pojo.CommentLike;
import org.example.pojo.Song;
import org.example.pojo.User;
import org.example.service.CommentService;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SongMapper songMapper;

    @Override
    public Result getCommentsBySongId(Integer songId, Integer page, Integer pageSize, String sortBy, Integer userId) {
        if (songId == null) {
            return Result.error("歌曲ID不能为空");
        }
        
        page = (page == null || page < 1) ? 1 : page;
        pageSize = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (page - 1) * pageSize;
        
        boolean sortByLikes = "popular".equals(sortBy);
        
        // 获取歌曲信息，检查是否存在
        Song song = songMapper.findById(songId);
        if (song == null) {
            return Result.error("歌曲不存在");
        }
        
        // 获取父评论列表
        List<Comment> comments = commentMapper.findBySongId(songId, offset, pageSize, sortByLikes);
        int total = commentMapper.countBySongId(songId);
        
        // 如果没有评论，直接返回空列表
        if (comments.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("comments", new ArrayList<>());
            data.put("total", 0);
            return Result.success(data);
        }
        
        // 获取评论ID列表
        List<Integer> commentIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        
        // 设置歌手是否是评论作者
        String artist = song.getArtist();
        for (Comment comment : comments) {
            String username = comment.getUsername();
            if (username != null && username.equals(artist)) {
                comment.setIsAuthor(true);
            } else {
                comment.setIsAuthor(false);
            }
            
            // 获取回复数量
            int replyCount = commentMapper.countRepliesByParentId(comment.getId());
            comment.setTotalReplies(replyCount);
            
            // 获取前3条回复
            if (replyCount > 0) {
                List<Comment> replies = commentMapper.findRepliesByParentId(comment.getId(), 0, 3);
                
                // 设置回复的作者标识
                for (Comment reply : replies) {
                    String replyUsername = reply.getUsername();
                    if (replyUsername != null && replyUsername.equals(artist)) {
                        reply.setIsAuthor(true);
                    } else {
                        reply.setIsAuthor(false);
                    }
                }
                
                comment.setReplies(replies);
            }
        }
        
        // 如果用户已登录，标记用户点赞状态
        if (userId != null) {
            List<Integer> likedCommentIds = commentLikeMapper.findCommentIdsByUserId(userId, commentIds);
            
            // 设置评论的点赞状态
            for (Comment comment : comments) {
                comment.setLiked(likedCommentIds.contains(comment.getId()));
                
                // 设置回复的点赞状态
                if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                    List<Integer> replyIds = comment.getReplies().stream()
                            .map(Comment::getId)
                            .collect(Collectors.toList());
                    
                    List<Integer> likedReplyIds = commentLikeMapper.findCommentIdsByUserId(userId, replyIds);
                    
                    for (Comment reply : comment.getReplies()) {
                        reply.setLiked(likedReplyIds.contains(reply.getId()));
                    }
                }
            }
        }
        
        // 返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("comments", comments);
        data.put("total", total);
        
        return Result.success(data);
    }

    @Override
    public Result getRepliesByCommentId(Integer commentId, Integer page, Integer pageSize, Integer userId) {
        if (commentId == null) {
            return Result.error("评论ID不能为空");
        }
        
        // 检查评论是否存在
        Comment parentComment = commentMapper.findById(commentId);
        if (parentComment == null) {
            return Result.error("评论不存在");
        }
        
        page = (page == null || page < 1) ? 1 : page;
        pageSize = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (page - 1) * pageSize;
        
        // 获取回复列表
        List<Comment> replies = commentMapper.findRepliesByParentId(commentId, offset, pageSize);
        
        // 获取歌曲信息
        Song song = songMapper.findById(parentComment.getSongId());
        String artist = song != null ? song.getArtist() : null;
        
        // 设置回复的作者标识
        for (Comment reply : replies) {
            String replyUsername = reply.getUsername();
            if (replyUsername != null && artist != null && replyUsername.equals(artist)) {
                reply.setIsAuthor(true);
            } else {
                reply.setIsAuthor(false);
            }
        }
        
        // 如果用户已登录，标记用户点赞状态
        if (userId != null && !replies.isEmpty()) {
            List<Integer> replyIds = replies.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toList());
            
            List<Integer> likedReplyIds = commentLikeMapper.findCommentIdsByUserId(userId, replyIds);
            
            for (Comment reply : replies) {
                reply.setLiked(likedReplyIds.contains(reply.getId()));
            }
        }
        
        return Result.success(replies);
    }

    @Override
    @Transactional
    public Result addComment(Integer userId, Integer songId, String content) {
        if (userId == null || songId == null || content == null || content.trim().isEmpty()) {
            return Result.error("参数错误");
        }
        
        // 检查用户是否存在
        if (userMapper.selectById(Long.valueOf(userId)) == null) {
            return Result.error("用户不存在");
        }
        
        // 检查歌曲是否存在
        if (songMapper.findById(songId) == null) {
            return Result.error("歌曲不存在");
        }
        
        // 创建评论对象
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setSongId(songId);
        comment.setContent(content.trim());
        comment.setLikeCount(0);
        
        // 保存评论
        int result = commentMapper.insert(comment);
        if (result > 0) {
            // 获取评论详情（包含用户信息）
            Comment createdComment = commentMapper.findCommentWithUserInfo(comment.getId());
            return Result.success(createdComment);
        } else {
            return Result.error("评论发表失败");
        }
    }

    @Override
    @Transactional
    public Result replyComment(Integer userId, Integer commentId, Integer replyToId, String content) {
        if (userId == null || commentId == null || content == null || content.trim().isEmpty()) {
            return Result.error("参数错误");
        }
        
        // 检查用户是否存在
        if (userMapper.selectById(Long.valueOf(userId)) == null) {
            return Result.error("用户不存在");
        }
        
        // 检查父评论是否存在
        Comment parentComment = commentMapper.findById(commentId);
        if (parentComment == null) {
            return Result.error("评论不存在");
        }
        
        // 检查回复用户是否存在
        if (replyToId != null && userMapper.selectById(Long.valueOf(replyToId)) == null) {
            return Result.error("回复的用户不存在");
        }
        
        // 创建回复评论对象
        Comment reply = new Comment();
        reply.setUserId(userId);
        reply.setSongId(parentComment.getSongId());
        reply.setContent(content.trim());
        reply.setParentId(commentId);
        reply.setReplyToId(replyToId);
        reply.setLikeCount(0);
        
        // 保存回复
        int result = commentMapper.insert(reply);
        if (result > 0) {
            // 获取回复详情（包含用户信息）
            Comment createdReply = commentMapper.findCommentWithUserInfo(reply.getId());
            return Result.success(createdReply);
        } else {
            return Result.error("回复发表失败");
        }
    }

    @Override
    @Transactional
    public Result likeComment(Integer userId, Integer commentId) {
        if (userId == null || commentId == null) {
            return Result.error("参数错误");
        }
        
        // 检查用户是否存在
        if (userMapper.selectById(Long.valueOf(userId)) == null) {
            return Result.error("用户不存在");
        }
        
        // 检查评论是否存在
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        
        // 检查是否已经点赞
        CommentLike existingLike = commentLikeMapper.findByUserIdAndCommentId(userId, commentId);
        if (existingLike != null) {
            return Result.error("已经点赞过该评论");
        }
        
        // 创建点赞记录
        CommentLike commentLike = new CommentLike();
        commentLike.setUserId(userId);
        commentLike.setCommentId(commentId);
        commentLike.setCreateTime(new Date());
        
        // 保存点赞记录
        int result = commentLikeMapper.insert(commentLike);
        
        // 增加评论点赞数
        if (result > 0) {
            commentMapper.updateLikeCount(commentId, 1);
            return Result.success("点赞成功");
        } else {
            return Result.error("点赞失败");
        }
    }

    @Override
    @Transactional
    public Result unlikeComment(Integer userId, Integer commentId) {
        if (userId == null || commentId == null) {
            return Result.error("参数错误");
        }
        
        // 检查用户是否存在
        if (userMapper.selectById(Long.valueOf(userId)) == null) {
            return Result.error("用户不存在");
        }
        
        // 检查评论是否存在
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        
        // 检查是否已经点赞
        CommentLike existingLike = commentLikeMapper.findByUserIdAndCommentId(userId, commentId);
        if (existingLike == null) {
            return Result.error("未点赞过该评论");
        }
        
        // 删除点赞记录
        int result = commentLikeMapper.deleteByUserIdAndCommentId(userId, commentId);
        
        // 减少评论点赞数
        if (result > 0) {
            commentMapper.updateLikeCount(commentId, -1);
            return Result.success("取消点赞成功");
        } else {
            return Result.error("取消点赞失败");
        }
    }

    @Override
    public List<Integer> getUserLikedCommentIds(Integer userId, List<Integer> commentIds) {
        if (userId == null || commentIds == null || commentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return commentLikeMapper.findCommentIdsByUserId(userId, commentIds);
    }
    
    @Override
    public List<Comment> getDirectCommentsBySongId(Integer songId, Integer page, Integer pageSize, String sortBy) {
        // 参数处理
        page = (page == null || page < 1) ? 1 : page;
        pageSize = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (page - 1) * pageSize;
        
        boolean sortByLikes = "popular".equals(sortBy);
        
        // 直接查询comments表，不关联用户表
        return commentMapper.findBySongIdDirect(songId, offset, pageSize, sortByLikes);
    }
    
    @Override
    public int countCommentsBySongId(Integer songId) {
        return commentMapper.countBySongId(songId);
    }

    @Override
    @Transactional
    public Result deleteComment(Integer userId, Integer commentId, boolean isAdmin) {
        // 查询评论是否存在
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        
        // 检查权限：只有评论作者或管理员可以删除评论
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            return Result.error("无权限删除该评论");
        }
        
        // 删除评论点赞记录
        commentLikeMapper.deleteByCommentId(commentId);
        
        // 如果是回复评论，更新原评论的回复数
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            commentMapper.decrementReplyCount(comment.getParentId());
        }
        
        // 删除该评论的所有回复
        List<Comment> replies = commentMapper.findRepliesByCommentId(commentId, 0, 1000);
        for (Comment reply : replies) {
            // 删除回复的点赞记录
            commentLikeMapper.deleteByCommentId(reply.getId());
            // 删除回复
            commentMapper.deleteById(reply.getId());
        }
        
        // 删除评论
        commentMapper.deleteById(commentId);
        
        return Result.success("评论删除成功");
    }
} 
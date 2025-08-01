package org.example.controller;

import org.example.pojo.Comment;
import org.example.service.CommentService;
import org.example.utils.JwtHelper;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private JwtHelper jwtHelper;
    
    /**
     * 获取歌曲评论列表 (需要token)
     */
    @GetMapping("/song/{songId}")
    public Result getSongComments(
            @PathVariable Integer songId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestHeader(value = "token", required = false) String token
    ) {
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        return commentService.getCommentsBySongId(songId, page, pageSize, sortBy, userId);
    }
    
    /**
     * 获取歌曲评论列表 (无需token验证，管理后台专用)
     */
    @GetMapping("/admin/song/{songId}")
    public Result getCommentsForAdmin(
            @PathVariable Integer songId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false, defaultValue = "latest") String sortBy
    ) {
        return commentService.getCommentsBySongId(songId, page, pageSize, sortBy, null);
    }
    
    /**
     * 获取评论回复列表 (需要token)
     */
    @GetMapping("/{commentId}/replies")
    public Result getCommentReplies(
            @PathVariable Integer commentId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestHeader(value = "token", required = false) String token
    ) {
        Integer userId = null;
        if (token != null && !token.isEmpty()) {
            userId = jwtHelper.getUserId(token);
        }
        
        return commentService.getRepliesByCommentId(commentId, page, pageSize, userId);
    }
    
    /**
     * 获取评论回复列表 (无需token验证，管理后台专用)
     */
    @GetMapping("/admin/{commentId}/replies")
    public Result getRepliesForAdmin(
            @PathVariable Integer commentId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        return commentService.getRepliesByCommentId(commentId, page, pageSize, null);
    }
    
    /**
     * 发表评论
     */
    @PostMapping("/add")
    public Result addComment(
            @RequestBody Map<String, Object> params,
            @RequestHeader("token") String token
    ) {
        Integer userId = jwtHelper.getUserId(token);
        if (userId == null) {
            return Result.error("未登录或登录已过期");
        }
        
        Integer songId = params.get("songId") != null ? Integer.valueOf(params.get("songId").toString()) : null;
        String content = params.get("content") != null ? params.get("content").toString() : null;
        
        if (songId == null || content == null || content.trim().isEmpty()) {
            return Result.error("参数错误");
        }
        
        return commentService.addComment(userId, songId, content);
    }
    
    /**
     * 回复评论
     */
    @PostMapping("/reply")
    public Result replyComment(
            @RequestBody Map<String, Object> params,
            @RequestHeader("token") String token
    ) {
        Integer userId = jwtHelper.getUserId(token);
        if (userId == null) {
            return Result.error("未登录或登录已过期");
        }
        
        Integer commentId = params.get("commentId") != null ? Integer.valueOf(params.get("commentId").toString()) : null;
        Integer replyToId = params.get("replyToId") != null ? Integer.valueOf(params.get("replyToId").toString()) : null;
        String content = params.get("content") != null ? params.get("content").toString() : null;
        
        if (commentId == null || content == null || content.trim().isEmpty()) {
            return Result.error("参数错误");
        }
        
        return commentService.replyComment(userId, commentId, replyToId, content);
    }
    
    /**
     * 点赞评论
     */
    @PostMapping("/like/{commentId}")
    public Result likeComment(
            @PathVariable Integer commentId,
            @RequestHeader("token") String token
    ) {
        Integer userId = jwtHelper.getUserId(token);
        if (userId == null) {
            return Result.error("未登录或登录已过期");
        }
        
        return commentService.likeComment(userId, commentId);
    }
    
    /**
     * 取消点赞
     */
    @PostMapping("/unlike/{commentId}")
    public Result unlikeComment(
            @PathVariable Integer commentId,
            @RequestHeader("token") String token
    ) {
        Integer userId = jwtHelper.getUserId(token);
        if (userId == null) {
            return Result.error("未登录或登录已过期");
        }
        
        return commentService.unlikeComment(userId, commentId);
    }
    
    /**
     * 直接获取歌曲评论（不关联用户表，后台管理使用）
     */
    @GetMapping("/direct/song/{songId}")
    public Result getDirectSongComments(
            @PathVariable Integer songId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false, defaultValue = "latest") String sortBy
    ) {
        System.out.println("直接获取歌曲评论, songId: " + songId + ", page: " + page + ", pageSize: " + pageSize);
        
        try {
            // 直接查询评论，不做其他关联
            List<Map<String, Object>> comments = new ArrayList<>();
            Map<String, Object> commentData = new HashMap<>();
            
            // 调用mapper直接查询comments表
            List<Comment> rawComments = commentService.getDirectCommentsBySongId(songId, page, pageSize, sortBy);
            int total = commentService.countCommentsBySongId(songId);
            
            System.out.println("查询到 " + rawComments.size() + " 条评论");
            
            // 转换评论数据
            for (Comment comment : rawComments) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", comment.getId());
                map.put("userId", comment.getUserId());
                map.put("songId", comment.getSongId());
                map.put("content", comment.getContent());
                map.put("parentId", comment.getParentId());
                map.put("replyToId", comment.getReplyToId());
                map.put("likeCount", comment.getLikeCount());
                map.put("createTime", comment.getCreateTime());
                
                // 添加一些默认用户信息，防止前端显示问题
                map.put("username", "用户" + comment.getUserId());
                map.put("userAvatar", null);
                map.put("isAuthor", false);
                map.put("liked", false);
                map.put("replies", new ArrayList<>());
                map.put("totalReplies", 0);
                
                comments.add(map);
            }
            
            commentData.put("comments", comments);
            commentData.put("total", total);
            
            return Result.success(commentData);
            
        } catch (Exception e) {
            System.err.println("获取评论失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户删除评论（需要登录，只能删除自己的评论）
     */
    @DeleteMapping("/{commentId}")
    public Result deleteComment(
            @PathVariable Integer commentId,
            @RequestHeader("token") String token
    ) {
        Integer userId = jwtHelper.getUserId(token);
        if (userId == null) {
            return Result.error("未登录或登录已过期");
        }
        
        return commentService.deleteComment(userId, commentId, false);
    }
    
    /**
     * 管理员删除评论（管理后台使用）
     */
    @DeleteMapping("/admin/{commentId}")
    public Result adminDeleteComment(
            @PathVariable Integer commentId,
            @RequestHeader("token") String token
    ) {
        Integer userId = jwtHelper.getUserId(token);
        if (userId == null) {
            return Result.error("未登录或登录已过期");
        }
        
        // 这里可以添加管理员权限检查，但为简化示例，直接传入isAdmin=true
        boolean isAdmin = true; // 在实际项目中，这里应该根据用户角色判断
        
        return commentService.deleteComment(userId, commentId, isAdmin);
    }
    
    /**
     * 直接删除评论（不需要token验证，前端测试用）
     */
    @DeleteMapping("/direct/{commentId}")
    public Result directDeleteComment(
            @PathVariable Integer commentId
    ) {
        // 为了安全起见，设置一个默认用户ID
        Integer defaultUserId = 999;
        // 直接以管理员身份删除评论
        boolean isAdmin = true;
        
        return commentService.deleteComment(defaultUserId, commentId, isAdmin);
    }
} 
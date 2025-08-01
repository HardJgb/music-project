package org.example.controller;

import org.example.pojo.User;
import org.example.service.UserService;
import org.example.utils.JwtHelper;
import org.example.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("user")
@CrossOrigin//解决跨域
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    // 从配置文件中获取图片存储路径
    @Value("${imageUpload.basePath}")
    private String uploadBasePath;
    
    @Value("${imageUpload.accessPath}")
    private String imageAccessPath;
    
    private static final String AVATAR_DIR = "avatars/";
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtHelper jwtHelper;
    
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        // 添加调试日志
        System.out.println("用户尝试登录: " + user.getUserName());
        
        Result result = userService.login(user);
        
        // 输出返回的响应数据
        System.out.println("登录响应: " + result);
        
        return result;
    }

    @PostMapping("register")
    public Result register(@RequestBody User user){
        Result result = userService.register(user);
        return result;
    }
    //获取用户信息接口
    @GetMapping("getUserInfo")
    public Result getuser(@RequestHeader String token){
    log.info("token:"+token);
        Result result = userService.getUserInfo(token);
        return  result;
    }
    
    // 新增接口路径，保持和前端一致
    @GetMapping("info")
    public Result getUserInfo(@RequestHeader String token){
        log.info("info接口 token:"+token);
        Result result = userService.getUserInfo(token);
        return  result;
    }
    
    @PostMapping("update")
    public Result updateUser(@RequestBody User user){
        Result result = userService.updateUserInfo(user);
        return result;
    }
    
    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file, @RequestHeader String token) {
        // 验证token
        Integer userId = jwtHelper.getUserId(token);
        if (userId == null) {
            return Result.error("未登录或token已过期");
        }
        
        try {
            // 确保目录存在
            File fullUploadDir = new File(uploadBasePath + AVATAR_DIR);
            if (!fullUploadDir.exists()) {
                fullUploadDir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            
            // 保存文件
            Path filePath = Paths.get(uploadBasePath + AVATAR_DIR + newFileName);
            Files.write(filePath, file.getBytes());
            
            // 更新用户头像路径 - 使用访问URL路径而不是文件系统路径
            String avatarUrl = imageAccessPath + AVATAR_DIR + newFileName;
            log.info("保存的头像URL: " + avatarUrl);
            
            User user = new User();
            user.setUid(userId);
            user.setAvatar(avatarUrl);
            
            Result result = userService.updateUserInfo(user);
            
            if (result.getCode() == 200) {
                return Result.success(avatarUrl);
            } else {
                return result;
            }
        } catch (IOException e) {
            log.error("上传头像失败", e);
            return Result.error("上传头像失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员获取用户列表
     */
    @GetMapping("/admin/users")
    public Result getUserList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "query", required = false) String query) {
        log.info("管理员获取用户列表: page={}, pageSize={}, query={}", page, query, pageSize);
        return userService.getUserList(page, pageSize, query);
    }
    
    /**
     * 管理员获取用户详情
     */
    @GetMapping("/admin/users/{userId}")
    public Result getUserDetail(@PathVariable Integer userId) {
        log.info("管理员获取用户详情: userId={}", userId);
        return userService.getUserDetail(userId);
    }
    
    /**
     * 管理员更新用户状态
     */
    @PutMapping("/admin/users/{userId}/status")
    public Result updateUserStatus(
            @PathVariable Integer userId,
            @RequestBody Map<String, Integer> statusMap) {
        Integer status = statusMap.get("status");
        log.info("管理员更新用户状态: userId={}, status={}", userId, status);
        return userService.updateUserStatus(userId, status);
    }
    
    /**
     * 管理员更新用户信息
     */
    @PutMapping("/admin/users/{userId}")
    public Result updateUserInfo(
            @PathVariable Integer userId,
            @RequestBody User user) {
        log.info("管理员更新用户信息: userId={}, user={}", userId, user);
        
        // 确保用户ID与路径中的ID一致
        user.setUid(userId);
        
        return userService.updateUserInfo(user);
    }
    
    /*接收参数方式
    @param -接收地址栏传参
    @RequestBody -接收json请求体传参
    @RequestHeader -接收请求头部传参
    @PathVariable -接收动态路径换传参
     */
}

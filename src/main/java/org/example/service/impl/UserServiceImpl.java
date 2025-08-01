package org.example.service.impl;

import com.alibaba.druid.util.StringUtils;
import org.example.mapper.UserMapper;
import org.example.pojo.User;
import org.example.service.UserService;

import org.example.utils.JwtHelper;
import org.example.utils.MD5Util;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtHelper jwtHelper;
    /*登录业务

     */
    @Override
    public Result login(User user){
        //查询用户是否存在
        User resultUser = userMapper.selectByName(user.getUserName());
        if(resultUser == null){
            return Result.error("用户名不存在");
        }

        //1.加密密码
        //2.对比密码
        String encryptPwd = MD5Util.encrypt((user.getUserPwd()));
        if(!StringUtils.isEmpty(user.getUserPwd())&& resultUser.getUserPwd().equals(encryptPwd)){
                //登陆成功 使用用户id生成token
                String token = jwtHelper.createToken(Long.valueOf(resultUser.getUid()));
                return Result.success(token);
        }

        return Result.error("密码错误");
    }

    /*注册业务

     */
    @Override
    public Result register(User user) {
        //0.检查用户名
        User resultUser = userMapper.selectByName(user.getUserName());
        if(resultUser != null){
            return Result.error("用户名已存在");
        }
        //1.加密密码
        String encryptPwd = MD5Util.encrypt(user.getUserPwd());
        user.setUserPwd(encryptPwd);
        //2.插入数据库
        userMapper.insertUser(user);
        return Result.success(user);
    }
    /*获取用户信息业务
     0.检验token
     1.将前端传递的token转为用户id
     2.查询数据库

     */
    @Override
    public Result getUserInfo(String token){
        //0.检验token
//        boolean expiration = jwtHelper.isExpiration(token);
//        if(expiration) {
//            return Result.error("token信息不正确或已过期");
//        }
        //1.将前端传递的token转为用户id
        Long userId = Long.valueOf(jwtHelper.getUserId(token));
        //2.查询数据库
        User user = userMapper.selectById(userId);
        return Result.success(user);
    }

    @Override
    public Result updateUserInfo(User user){
        // 如果更新包含密码，需要进行加密
        if (user.getUserPwd() != null && !user.getUserPwd().isEmpty()) {
            String encryptPwd = MD5Util.encrypt(user.getUserPwd());
            user.setUserPwd(encryptPwd);
        }
        
        userMapper.updateUser(user);
        return Result.success(null, "修改成功");
    }
    
    @Override
    public Result getUserList(int page, int pageSize, String query) {
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 获取用户列表
        List<User> users = userMapper.getUserList(offset, pageSize, query);
        
        // 获取符合条件的总用户数
        int total = userMapper.getUserCount(query);
        
        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("users", users);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        
        return Result.success(data);
    }
    
    @Override
    public Result getUserDetail(Integer userId) {
        User user = userMapper.selectById(Long.valueOf(userId));
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }
    
    @Override
    public Result updateUserStatus(Integer userId, Integer status) {
        // 检查用户是否存在
        User user = userMapper.selectById(Long.valueOf(userId));
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 更新用户状态
        user.setStatus(status);
        userMapper.updateUser(user);
        
        return Result.success(null, "状态更新成功");
    }
}

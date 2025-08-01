package org.example.service;

import org.apache.ibatis.annotations.Param;
import org.example.pojo.User;
import org.example.utils.Result;


public interface UserService {
    //登陆业务
    Result login(User user);
    //注册业务
    Result register(User user);

    //根据token获取用户数据
    Result getUserInfo(String token);


    Result updateUserInfo(User user);
    
    // 获取用户列表
    Result getUserList(int page, int pageSize, String query);
    
    // 获取用户详情
    Result getUserDetail(Integer userId);
    
    // 更新用户状态
    Result updateUserStatus(Integer userId, Integer status);
}

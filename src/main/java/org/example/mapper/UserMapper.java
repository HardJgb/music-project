package org.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.pojo.User;

import java.util.List;

/**
 * 接口里只定义操作数据库方法
 *  - 返回值类型
 *  - 方法名称
 *  - 参数
 */
public interface UserMapper {
    // 根据id查询用户信息
    User selectById(Long id);

    User selectByName(String name);
    // 插入员工信息
    int insertUser(User user);
    // 根据用户id修改用户
    int updateUser(User user);
    // 根据用户id删除用户

    int delUser(int id);
    //查询全部用户
    List<User> selectAll();
    
    // 分页查询用户列表
    List<User> getUserList(@Param("offset") int offset, @Param("pageSize") int pageSize, @Param("query") String query);
    
    // 获取符合条件的用户总数
    int getUserCount(@Param("query") String query);
}

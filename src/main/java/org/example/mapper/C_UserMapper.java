package org.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.pojo.C_User;
import org.example.pojo.User;

import java.util.List;

public interface C_UserMapper {
    // 根据id查询用户信息
    C_User selectByC_Id(int id);
    // 插入员工信息
    int insertC_User(C_User c_user);
    // 根据用户id修改用户姓名
    int updateC_Name(@Param("id") int id, @Param("userName") String name);
    // 根据用户id删除用户
    int delC_User(int id);
    //查询全部用户
    List<C_User> selectAllC();
}

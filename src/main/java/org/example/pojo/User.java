package org.example.pojo;

import lombok.Data;

@Data
public class User {
//    private String name;
//    private int age;
//    private String gender;
    //  getter  setter

    private int uid;
    private String userName;
    private String userPwd;
    private String avatar;
    private Integer status; // 用户状态: 0-禁用, 1-正常
}

package org.example.controller;

import org.example.mapper.C_UserMapper;
import org.example.mapper.UserMapper;
import org.example.pojo.C_User;
import org.example.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController  // 复合注解  ResponseBody+Controller
//@ResponseBody  // 以json形式返回数据
//@Controller  // 将类加入ioc容器中
public class HelloController {
    // 向浏览器输出“Hello Springboot3!”
    // @GetMapping  get请求方式
//    @RequestMapping  接口方法加入到handlerMapping里面去  GET  POST  PUT  DELETE

    /**
     * SpringMVC-
     * 参数接收：
     *  1、param（地址栏传参）参数接收
     *      - 直接接收（接收参数名和参数数据类型与地址栏的传参保持一致）
     *      - @RequestParam  （设置必填、默认值、参数名重新命名）
     *  2、动态路径传参（地址栏没有键名的传参）
     *      - 接口地址后写键名：{键名}
     *      - 使用@PathVariable 接收数据
     *  3、json参数接收
     *      - 使用@RequestBody 接收数据
     * 参数响应：
     *  @ResponseBody  // 以json形式返回数据
     *  静态资源放在默认路径下即可（META-INF/resources、public、resources、static）
     */
//    @GetMapping("hello")
//    public String hello(int id,String name){
//        System.out.println("id = " + id);
//        System.out.println("name = " + name);
//        return "Hello Springboot3!";
//    }
//    @GetMapping("hello")
//    public String hello(@RequestParam(value="id",required = true) int userId,
//                        @RequestParam(value = "name",required = false,defaultValue = "admin") String userName){
//        System.out.println("id = " + userId);
//        System.out.println("name = " + userName);
//        return "Hello Springboot3!";
//    }

   //用户
    @GetMapping("hello/{id}/{name}")
    public String hello(@PathVariable int id,@PathVariable String name){
        System.out.println("id = " + id);
        System.out.println("name = " + name);
        return "Hello Springboot3!";
    }

    @PostMapping("data")
    public String data(@RequestBody User user){
        System.out.println("user = " + user);
        return "接收成功";
    }

    @Autowired
    private UserMapper userMapper;

    @GetMapping("getUser")
    public User getuser(Long id){
        return userMapper.selectById((long) Math.toIntExact(id));
    }

    @PostMapping("saveUser")
    public String saveuser(@RequestBody User user){
        userMapper.insertUser(user);
        return "插入成功 主键id="+user.getUid();
    }

    @GetMapping("updateUser")
    public String update(User user){
        userMapper.updateUser(user);
        return "修改成功";
    }
    @DeleteMapping("delUser")
    public String del(int id){
        userMapper.delUser(id);
        return "删除成功";
    }

    @GetMapping("selectAll")
    public List<User> selectAll(){
        return userMapper.selectAll();

    }








    //作业
    @Autowired
    private C_UserMapper c_userMapper;

    @GetMapping("getCUser")
    public C_User getCUser(int id){
        return c_userMapper.selectByC_Id(id);
    }

    @PostMapping("saveCUser")
    public String saveCUser(@RequestBody C_User c_user){
        System.out.println("c_user = " + c_user);
        c_userMapper.insertC_User(c_user);
        return "插入成功 主键id="+c_user.getId();
    }

    @GetMapping("updateCUser")
    public String upCDate(int id,String name){
        c_userMapper.updateC_Name(id,name);
        return "修改成功";
    }
    @DeleteMapping("delCUser")
    public String delC(int id){
        c_userMapper.delC_User(id);
        return "删除成功";
    }

    @GetMapping("selectAllC")
    public List<C_User> selectAllC(){
        return c_userMapper.selectAllC();

    }

}

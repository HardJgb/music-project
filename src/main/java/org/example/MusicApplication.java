package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // 标识启动类
@MapperScan("org.example.mapper") // 扫描mapper接口
public class MusicApplication {
    public static void main(String[] args) {
        // springboot工程的启动方法
        SpringApplication.run(MusicApplication.class,args);
    }
}

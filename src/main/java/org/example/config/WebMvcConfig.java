package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源映射 - 图片和音频文件都在同一目录下
        registry.addResourceHandler("/public/image/**")
                .addResourceLocations("file:C:/Users/32143/Desktop/接单写的/新建文件夹/全栈音乐播放平台/Music-project/src/main/resources/static/image/");
    }
} 
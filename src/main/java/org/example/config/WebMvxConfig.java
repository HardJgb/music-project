package org.example.config;

import org.example.interceptor.LoginProtectInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvxConfig implements WebMvcConfigurer {
//    //登录拦截器
//    @Autowired
//    private LoginProtectInterceptor loginProtectInterceptor;
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        //addPathPatterns("/common/request/one") 添加拦截路径
//        //excludePathPatterns("/common/request/tow"); 排除路径,排除应该在拦截的范围内
//        registry.addInterceptor(loginProtectInterceptor).excludePathPatterns("/user/login","/user/register","/public/image/**");
//
//    }
    //图片拦截器
    @Value("${imageUpload.basePath}")
    private String basePath;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) { //图片地址映射
        registry.addResourceHandler("/public/image/**").addResourceLocations("file:"+basePath);
    }
    //做完图片上传之后，根据返回的路径访问不到图片，重启了服务器，再次访问发现竟然可以了，不重启它就不能显示。
    //这是因为对服务器的保护措施导致的，服务器不能对外部暴露真实的资源路径，需要配置虚拟路径映射访问。

}

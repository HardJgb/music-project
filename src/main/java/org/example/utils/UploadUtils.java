package org.example.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;import java.util.UUID;
@Component
public class UploadUtils {
    //图片上传位置，静态资源文件夹文件存储路径
    @Value("${imageUpload.basePath}")
    private String BASE_PATH;

    //访问路径
    //在浏览器上访问该路径可以看到图片
    @Value("${imageUpload.accessPath}")
    private String ACCESS_PATH;

    /**
     * 图片上传
     * @param file
     * @return 访问图片的网址
     */
    public String upload(MultipartFile file){
        //获取图片的原始名称
        String fileName = file.getOriginalFilename();
        //截取后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        // 生成新名称
        //UUID.randomUUID() 生成随机字符串
        String newFileName = UUID.randomUUID()+suffixName;
        // 创建一个File对象
        File dest =new File(BASE_PATH+newFileName);
        // 检查这个File的父目录是否已存在
        if (!dest.getParentFile().exists()) {
            // 不存在则使用mkdirs()创建  这个方法会创建必要的所有上级目录。
            dest.getParentFile().mkdirs();
        }
        try {
            //将`file`的内容移动到`dest`指定的位置
            file.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 返回图片访问地址
        return ACCESS_PATH + newFileName;
    }
}

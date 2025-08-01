package org.example.utils;

import java.io.File;

public class FileUtils {
    
    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 扩展名（不包含点号）
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty() || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    
    /**
     * 检查文件是否为图片
     * @param fileName 文件名
     * @return 是否为图片
     */
    public static boolean isImage(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif") || 
               extension.equals("bmp") || extension.equals("webp");
    }
    
    /**
     * 创建目录（如果不存在）
     * @param directory 目录路径
     * @return 是否成功创建
     */
    public static boolean createDirectoryIfNotExists(String directory) {
        File dir = new File(directory);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }
} 
package org.example.service.impl;

import org.example.service.PublicService;
import org.example.utils.Result;
import org.example.utils.UploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PublicServiceImpl implements PublicService {
    @Autowired
    private UploadUtils uploadUtils;

    /**
     * 图片上传业务
     * @pararm file
     * @return
     */
    @Override
    public Result uploadImage(MultipartFile file){
        String upload = uploadUtils.upload(file);
        return Result.success(upload);
    }
}

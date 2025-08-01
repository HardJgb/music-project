package org.example.service;

import org.example.utils.Result;
import org.springframework.web.multipart.MultipartFile;

public interface PublicService {
    //图片上传
    Result uploadImage(MultipartFile file);

}

package org.example.controller;

import org.example.service.PublicService;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("public")
public class PublicController {
    @Autowired
    private PublicService publicService;
    //图片上传
    @PostMapping("image")
    public Result uploadImage(@RequestBody MultipartFile file){
        Result result = publicService.uploadImage(file);
        return result;
    }
}

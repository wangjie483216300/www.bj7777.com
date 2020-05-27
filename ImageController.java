package com.jst.imageservice.modules.controller;

import com.jst.imageservice.common.utils.FileUtilService;
import com.jst.imageservice.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * @author qlx
 * @date 2019/12/26 9:52
 * @description
 **/
@RestController
@RequestMapping("/imageservice")
@Slf4j
public class ImageController {


    @Autowired
    private FileUtilService fileUtilService;
    /**
     * 图片上传
     * */
    @RequestMapping("/fileUpload")
    @ResponseBody //@ResponseBody的作用其实是将java对象转为json格式的数据
    public R fileUpload(@RequestParam("file_tFile") MultipartFile file){
        try {
            Map<String, Object> paraMap = fileUtilService.downLoadImg(file);
            fileUtilService.compressImg(file);
            return R.ok().add(paraMap);
        } catch (IOException e) {
            e.printStackTrace();
            return R.error();
        }
    }
}

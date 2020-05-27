package com.jst.imageservice.common.utils;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: lxp
 * @Description:文件util类
 * @Date: Create in 15:48 2018/12/21
 */
@Configuration
@Slf4j
public class FileUtilService {
    @Value("${dcresource.storePath}")
    private String baseImagePath;

    @Value("${dcresource.compresspath}")
    private String compresspath;

    /**
     * base64转图片
     * @param base64Data
     * @return
     */
    public String base64ToFile(String base64Data){
        String commonPath =  DateUtils.getToday().replaceAll("-","")+"/";
        String orignalFileName = DateUtils.getUUID()+".png";
        commonPath+=orignalFileName;
        try {
            log.info("start recoding images...【{}】",commonPath);
            byte[] bytes = Base64.decodeBase64(base64Data);
            FileUtils.writeByteArrayToFile(new File(baseImagePath+commonPath),bytes);//根据 parent 路径名字符串和 child 路径名字符串创建一个新 File 实例。
            log.info("【{}】store success.",commonPath);
            return "/static/images/"+commonPath;
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return "";
        }
    }

    /**
     * 上传图片
     * @param file
     * @return
     * @throws IOException
     */
    public Map<String,Object> downLoadImg(MultipartFile file) throws IOException {
        //当天日期作为文件夹
        String commonPath = DateUtils.getToday().replaceAll("-","")+"/";//日期
        String originalFileName = file.getOriginalFilename();//file.getOriginalFilename()是得到上传时的文件名
        String[] suffix = originalFileName.split("\\.");//分割
        originalFileName = DateUtils.getUUID()+"."+suffix[suffix.length-1];//获取文件名
        commonPath+=originalFileName;//文件路径

        //下载图片到本地
        FileUtils.copyInputStreamToFile(file.getInputStream(),new File(baseImagePath+commonPath));//输入流,文件目的地
        System.out.println(baseImagePath+commonPath);
        Map<String,Object> paramMap =new HashMap<>();
        String url = "/static/images/"+commonPath;//文件路径url
        Long size = file.getSize();//获取文件长度
        log.info("图片存储成功{}",url);
        paramMap.put("url",url);
        paramMap.put("size",size);
        return paramMap;
    }
    /**
     * 压缩图片
     * @param file
     * @return
     * @throws IOException
     */
    public void compressImg(MultipartFile file) throws IOException {
            String commonPath = DateUtils.getToday().replaceAll("-","")+"/";//日期
            String originalFileName = file.getOriginalFilename();//file.getOriginalFilename()是得到上传时的文件名
            String[] suffix = originalFileName.split("\\.");//分割
            originalFileName = DateUtils.getUUID()+"."+suffix[suffix.length-1];//获取文件名
            commonPath+=originalFileName;//文件路径
            String[] s = commonPath.split("/");
            //压缩图片到本地
            File file1 = new File(compresspath+s[0]);
            file1.mkdirs();
            file1=new File(compresspath+commonPath);
            file1.createNewFile();
//            OutputStream out = new FileOutputStream(file1);
                try {
                    Thumbnails.of(file.getInputStream())
                            //.size(200, 300)  指定大小
                            //rotate(-90/90)   旋转
                            .scale(0.7f) //    按照比例进行缩放
                            .outputQuality(0.7) // 控制图片质量
                            .outputFormat("jpg") // 转化图像格式
                            .toFile(compresspath+commonPath);
//                            .toOutputStream(out);//输出流
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }

}

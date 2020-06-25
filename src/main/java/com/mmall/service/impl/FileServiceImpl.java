package com.mmall.service.impl;

import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path){
        String fileName = file.getOriginalFilename();
        String fileNameExtention = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileNameExtention;
        logger.info("开始上传文件，文件名为{}, 上传的路径为{}, 新文件名为{}", fileName, path, uploadFileName);

        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);

        try {
            file.transferTo(targetFile);
            // 到这里文件已经成功上传到tomcat的文件夹(/upload)下

            // finished 将targetFile上传到我们的FTP服务器上
            //List<File> list = new ArrayList<>();
            //list.add(targetFile);
            //FTPUtil.uploadFile(list);
            // finished 上传完之后，删除tomcat文件夹下的图片
            //targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件失败");
            return null;
        }

        return targetFile.getName();

    }
}

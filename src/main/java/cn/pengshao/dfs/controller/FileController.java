package cn.pengshao.dfs.controller;

import cn.pengshao.dfs.config.PsdfsConfigProperties;
import cn.pengshao.dfs.constants.Constants;
import cn.pengshao.dfs.model.FileMeta;
import cn.pengshao.dfs.syncer.HttpSyncer;
import cn.pengshao.dfs.syncer.MqSyncer;
import cn.pengshao.dfs.utils.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * file download and upload controller.
 *
 * @Author: yezp
 * @date 2024/7/14 21:38
 */
@Slf4j
@RestController
public class FileController {

    @Autowired
    HttpSyncer httpSyncer;
    @Autowired
    MqSyncer mqSyncer;
    @Autowired
    PsdfsConfigProperties properties;

    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file, HttpServletRequest request) {
        // 1、处理文件
        String fileName = request.getHeader(Constants.X_FILE_NAME);
        String originalFileName = file.getOriginalFilename();
        boolean sync = false;
        if (fileName == null || fileName.isEmpty()) {
            // 如果这个为空则是正常上传
            fileName = FileUtils.getUUIDFile(file.getOriginalFilename());
            sync = true;
        } else {
            // 如果这个不为空则是同步上传
            String syncOriginalFileName = request.getHeader(Constants.X_ORIGINAL_NAME);
            if (syncOriginalFileName != null && !syncOriginalFileName.isEmpty()) {
                originalFileName = syncOriginalFileName;
            }
        }

        String subDir = FileUtils.getSubDir(fileName);
        File dest = new File(properties.getUploadPath() + "/" + subDir + "/" + fileName);
        file.transferTo(dest);
        long fileSize = file.getSize();
        log.info("upload file originalFileName:{}, size:{}", originalFileName, fileSize);

        // 2、处理meta
        FileMeta meta = new FileMeta(fileName, originalFileName, fileSize, properties.getDownloadUrl());
        if (properties.isAutoMd5()) {
            meta.getTags().put(Constants.MD5, DigestUtils.md5DigestAsHex(new FileInputStream(dest)));
        }

        // 2.1 存放到本地文件
        String metaName = fileName + ".meta";
        File metaFile = new File(properties.getUploadPath() + "/" + subDir + "/" + metaName);
        FileUtils.writeMeta(metaFile, meta);

        // 2.2 存到数据库
        // 2.3 存放到配置中心或注册中心，比如zk
        // 3. 同步到backup
        if (sync) {
            if (properties.isSyncBackup()) {
                try {
                    httpSyncer.sync(dest, properties.getBackupUrl(), originalFileName);
                } catch (Exception e){
                    log.warn("http sync fail.", e);
                    mqSyncer.sync(meta);
                }
            } else {
                mqSyncer.sync(meta);
            }
        }
        return fileName;
    }

    @RequestMapping("/download")
    public void download(@RequestParam String name, HttpServletResponse response) {
        try {
            String subDir = FileUtils.getSubDir(name);
            String path = properties.getUploadPath() + "/" + subDir + "/" + name;
            File file = new File(path);
            log.info(" ==> download file:{}", file.getPath());

            response.setCharacterEncoding("UTF-8");
            response.setContentType(FileUtils.getMimeType(name));
            // response.setHeader("Content-Disposition", "attachment;filename=" + name);
            response.setHeader("Content-Length", String.valueOf(file.length()));
            FileUtils.output(file, response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("/meta")
    public String meta(String name) {
        String subDir = FileUtils.getSubDir(name);
        String path = properties.getUploadPath() + "/" + subDir + "/" + name + ".meta";
        File file = new File(path);
        try {
            return FileCopyUtils.copyToString(new FileReader(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

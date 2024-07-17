package cn.pengshao.dfs.controller;

import cn.pengshao.dfs.model.FileMeta;
import cn.pengshao.dfs.syncer.FileSyncer;
import cn.pengshao.dfs.utils.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;

/**
 * file download and upload controller.
 *
 * @Author: yezp
 * @date 2024/7/14 21:38
 */
@Slf4j
@RestController
public class FileController {

    @Value("${psdfs.path}")
    private String uploadPath;

    @Value("${psdfs.isSync}")
    private boolean isSync;

    @Value("${psdfs.backupUrl}")
    private String backupUrl;

    @Value("${psdfs.autoMd5}")
    private boolean autoMd5;

    @Autowired
    FileSyncer syncer;

    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file, HttpServletRequest request) {
        // 1、处理文件
        String fileName = request.getHeader("X-Filename");
        boolean sync = false;
        if (fileName == null || fileName.isEmpty()) {
            fileName = FileUtils.getUUIDFile(file.getOriginalFilename());
            sync = true;
        }
        String subDir = FileUtils.getSubDir(fileName);
        File dest = new File(uploadPath + "/" + subDir + "/" + fileName);
        file.transferTo(dest);

        String originalFileName = file.getOriginalFilename();
        long fileSize = file.getSize();
        log.info("upload file originalFileName:{}, size:{}", originalFileName, fileSize);

        // 2、处理meta
        FileMeta meta = new FileMeta();
        meta.setName(fileName);
        meta.setOriginalFileName(originalFileName);
        meta.setSize(fileSize);
        if (autoMd5) {
            meta.getTags().put("md5", DigestUtils.md5DigestAsHex(new FileInputStream(dest)));
        }

        // 2.1 存放到本地文件
        String metaName = fileName + ".meta";
        File metaFile = new File(uploadPath + "/" + subDir + "/" + metaName);
        FileUtils.writeMeta(metaFile, meta);

        // 2.2 存到数据库
        // 2.3 存放到配置中心或注册中心，比如zk

        // 3. 同步到backup
        // 同步文件到backup
        if (sync) {
            syncer.sync(dest, backupUrl, isSync);
        }
        return fileName;
    }

    @RequestMapping("/download")
    public void download(@RequestParam String name, HttpServletResponse response) {
        try {
            String subDir = FileUtils.getSubDir(name);
            String path = uploadPath + "/" + subDir + "/" + name;
            File file = new File(path);
            log.info(file.getPath());
            String fileName = file.getName();

            // 将文件写入输入流
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream in = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[16 * 1024];

            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Disposition", "attachment;filename="
                    + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");

            while (in.read(buffer) != -1) {
                outputStream.write(buffer);
            }
            in.close();
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("/meta")
    public String meta(String name) {
        String subDir = FileUtils.getSubDir(name);
        String path = uploadPath + "/" + subDir + "/" + name + ".meta";
        File file = new File(path);
        try {
            return FileCopyUtils.copyToString(new FileReader(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package cn.pengshao.dfs.controller;

import cn.pengshao.dfs.syncer.FileSyncer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 * Description:
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

    @Autowired
    FileSyncer syncer;

    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file, HttpServletRequest request) {
        String originalFileName = file.getOriginalFilename();
        String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = request.getHeader("X-Filename");
        boolean sync = false;
        if (fileName == null || fileName.isEmpty()) {
            fileName = UUID.randomUUID() + ext;
            sync = true;
        }

        String dir = fileName.substring(0, 2);
        log.info("upload file originalFileName:{}, size:{}", originalFileName, file.getSize());
        File dest = new File(uploadPath + "/" + dir + "/" + fileName);
        file.transferTo(dest);
        if (sync) {
            syncer.sync(dest, backupUrl, isSync);
        }
        return fileName;
    }

    @RequestMapping("/download")
    public void download(@RequestParam String name, HttpServletResponse response) {
        try {
            String dir = name.substring(0, 2);
            String path = uploadPath + "/" + dir + "/" + name;
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

            while (in.read(buffer) > 0) {
                outputStream.write(buffer);
            }
            in.close();
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

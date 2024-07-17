package cn.pengshao.dfs.utils;

import cn.pengshao.dfs.model.FileMeta;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/7/17 21:23
 */
public class FileUtils {

    static String DEFAULT_MIME_TYPE = "application/octet-stream";

    public static String getMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String content = fileNameMap.getContentTypeFor(fileName);
        return content == null ? DEFAULT_MIME_TYPE : content;
    }

    public static void init(String uploadPath) {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 输出256个文件夹，名称为十六进制
        for (int i = 0; i < 256; i++) {
            String subDir = String.format("%02x", i);
            File dirPath = new File(uploadPath, subDir);
            if (!dirPath.exists()) {
                dirPath.mkdirs();
            }
        }
    }

    public static String getUUIDFile(String file) {
        return UUID.randomUUID() + getExt(file);
    }

    public static String getSubDir(String file) {
        return file.substring(0, 2);
    }

    public static String getExt(String originalFileName) {
        return originalFileName.substring(originalFileName.lastIndexOf("."));
    }

    @SneakyThrows
    public static void writeMeta(File metaFile, FileMeta meta) {
        String json = JSON.toJSONString(meta);
        Files.writeString(Paths.get(metaFile.getAbsolutePath()), json,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }
}

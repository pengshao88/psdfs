package cn.pengshao.dfs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * file meta data.
 *
 * @Author: yezp
 * @date 2024/7/17 21:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMeta {

    private String name;
    private String originalFileName;
    private long size;
    private String downloadUrl;
    private Map<String, String> tags = new HashMap<>();

    public FileMeta(String name, String originalFileName, long size, String downloadUrl) {
        this.name = name;
        this.originalFileName = originalFileName;
        this.size = size;
        this.downloadUrl = downloadUrl;
    }
}

package cn.pengshao.dfs.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/7/20 7:04
 */
@Data
@ConfigurationProperties(prefix = "psdfs")
public class PsdfsConfigProperties {

    private String uploadPath;
    private String backupUrl;
    private String downloadUrl;
    private String group;
    private String topic;
    private boolean autoMd5;
    private boolean syncBackup;

}

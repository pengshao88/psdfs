package cn.pengshao.dfs.syncer;

import cn.pengshao.dfs.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * http sync file.
 *
 * @Author: yezp
 * @date 2024/7/14 21:54
 */
@Slf4j
@Component
public class HttpSyncer {

    public boolean sync(File file, String backupUrl, String originalName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Constants.X_FILE_NAME, file.getName());
        headers.set(Constants.X_ORIGINAL_NAME, originalName);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity = new HttpEntity<>(builder.build(), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(backupUrl, httpEntity, String.class);
        log.info("sync result = " + responseEntity.getBody());
        return true;
    }
}

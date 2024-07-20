package cn.pengshao.dfs.syncer;

import cn.pengshao.dfs.config.PsdfsConfigProperties;
import cn.pengshao.dfs.model.FileMeta;
import cn.pengshao.dfs.utils.FileUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * MQ sync file.
 *
 * @Author: yezp
 * @date 2024/7/20 7:26
 */
@Slf4j
@Component
public class MqSyncer {

    @Autowired
    PsdfsConfigProperties properties;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    public void sync(FileMeta fileMeta) {
        String json = JSON.toJSONString(fileMeta);
        Message<String> message = MessageBuilder.withPayload(json).build();
        rocketMQTemplate.send(properties.getTopic(), message);
        log.info("send mq success, fileMeta:{}", json);
    }

    @Service
    @RocketMQMessageListener(topic = "${psdfs.topic}", consumerGroup = "${psdfs.group}")
    public class FileMQSyncer implements RocketMQListener<MessageExt> {

        @Override
        public void onMessage(MessageExt message) {
            // 1. 从消息里拿到meta数据
            log.info(" ==> onMessage ID = " + message.getMsgId());
            String json = new String(message.getBody());
            log.info(" ==> message json = " + json);
            FileMeta meta = JSON.parseObject(json, FileMeta.class);
            String downloadUrl = meta.getDownloadUrl();
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                log.info(" ==> downloadUrl is empty.");
                return;
            }

            // 2.去重本机操作
            String localUrl = properties.getDownloadUrl();
            if (localUrl.equals(downloadUrl)) {
                log.info(" ====> the same file server, ignore mq sync task.");
                return;
            }
            log.info(" ====> the other file server, process mq sync task.");

            // 2. 写meta文件
            String dir = properties.getUploadPath() + "/" + meta.getName().substring(0, 2);
            File metaFile = new File(dir, meta.getName() + ".meta");
            if(metaFile.exists()) {
                log.info(" ==> meta file exists and ignore save:{}}", metaFile.getAbsolutePath());
            } else {
                log.info(" ==> meta file save:{}", metaFile.getAbsolutePath());
                FileUtils.writeString(metaFile, json);
            }

            // 3. 下载文件
            File file = new File(dir, meta.getName());
            if(file.exists() && file.length() == meta.getSize()) {
                log.info(" ==> file exists and ignore download:{}", file.getAbsolutePath());
                return;
            }

            String download = downloadUrl + "?name=" + file.getName();
            FileUtils.download(download, file);
        }
    }

}

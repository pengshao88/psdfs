package cn.pengshao.dfs;

import cn.pengshao.dfs.config.PsdfsConfigProperties;
import cn.pengshao.dfs.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/7/14 21:28
 */
@Slf4j
@SpringBootApplication
@Import(RocketMQAutoConfiguration.class)
@EnableConfigurationProperties(PsdfsConfigProperties.class)
public class PsdfsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsdfsApplication.class, args);
    }

    @Value("${psdfs.uploadPath}")
    private String uploadPath;

    @Bean
    ApplicationRunner runner() {
        return args -> {
            log.info("init psdfs dirs...");
            FileUtils.init(uploadPath);
        };
    }

}

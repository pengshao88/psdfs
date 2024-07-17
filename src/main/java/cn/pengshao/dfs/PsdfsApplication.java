package cn.pengshao.dfs;

import cn.pengshao.dfs.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/7/14 21:28
 */
@SpringBootApplication
public class PsdfsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsdfsApplication.class, args);
    }

    @Value("${psdfs.path}")
    private String uploadPath;

    @Bean
    ApplicationRunner runner() {
        return args -> {
            System.out.println("init psdfs dirs...");
            FileUtils.init(uploadPath);
        };
    }

}

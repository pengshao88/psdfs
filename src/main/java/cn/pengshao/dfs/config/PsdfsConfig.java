package cn.pengshao.dfs.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/7/17 21:22
 */
@Configuration
public class PsdfsConfig {

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation("D:\\tmp\\tomcat");
        return factory.createMultipartConfig();
    }

}

package com.example.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 이 설정이 있어야 브라우저에서 /images/aaa.jpg 로 접속했을 때 D:/shop/images/aaa.jpg를 보여줌
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${shop.image.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // application.properties의 경로를 기반으로 리소스 핸들러 설정
        // 예: ./uploads/images -> file:./uploads/images/
        String location = "file:" + (uploadPath.endsWith("/") ? uploadPath : uploadPath + "/");
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);

        // favicon 에러 방지용 설정 추가
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }

}
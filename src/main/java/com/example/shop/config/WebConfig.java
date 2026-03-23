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
        // 상대 경로(./uploads/images)를 절대 경로로 변환하여 윈도우 환경에서 안정적인 파일 서빙 지원
        String absolutePath = new java.io.File(uploadPath).getAbsolutePath().replace("\\", "/");
        if (!absolutePath.endsWith("/")) {
            absolutePath += "/";
        }
        String location = "file:///" + absolutePath;
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);

        // favicon 에러 방지용 설정 추가
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }

}
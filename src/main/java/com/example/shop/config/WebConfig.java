package com.example.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application-prod.properties 에 정의된 물리적 경로 주입
    @Value("${shop.image.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 외부 디렉토리(Docker 볼륨 경로) 매핑 시 'file:' 접두사 사용
        registry.addResourceHandler("/images/shop/**")
                .addResourceLocations("file:" + uploadPath + "/");

        // favicon 에러 방지용 설정 추가
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }
}
package com.example.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 이 설정이 있어야 브라우저에서 /images/aaa.jpg 로 접속했을 때 D:/shop/images/aaa.jpg를 보여줌
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 요청 URL 패턴: /images/shop/banner.png 등
        // 2. 실제 파일 위치: file:/app/resources/static/images/shop/
        // 반드시 경로 끝에 '/'를 붙여주세요.
        registry.addResourceHandler("/images/shop/**")
                .addResourceLocations("file:/app/resources/static/images/shop/");

        // favicon 에러 방지용 설정 추가
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }

}
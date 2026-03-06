package com.example.shop;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(
    basePackages = "com.example.shop.repository", 
    annotationClass = Mapper.class  // 핵심: @Mapper 어노테이션이 붙은 클래스만 MyBatis로 등록
)
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
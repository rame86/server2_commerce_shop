package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication
// public class ShopApplication {

// 	public static void main(String[] args) {
// 		SpringApplication.run(ShopApplication.class, args);
// 	}

// }

// ShopApplication.java
// @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}) // DB 자동설정 제외
// public class ShopApplication {
//     public static void main(String[] args) {
//         SpringApplication.run(ShopApplication.class, args);
//     }
// }
@SpringBootApplication
@MapperScan("com.example.shop.repository") // ShopMapper가 있는 패키지 경로를 지정
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}